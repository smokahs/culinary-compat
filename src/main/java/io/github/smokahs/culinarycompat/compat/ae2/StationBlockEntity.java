package io.github.smokahs.culinarycompat.compat.ae2;

import java.util.List;

import javax.annotation.Nullable;

import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.helpers.IPriorityHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.Platform;
import io.github.smokahs.culinarycompat.registry.LootFunctions;

public class StationBlockEntity extends BalmBlockEntity implements IStorageProvider, MenuProvider, IPriorityHost {

	private static final String NBT_MODE = "Mode";
	private static final String NBT_PRIORITY = "Priority";
	private static final String NBT_VIEW = "ViewMode";

	private final ItemProvider itemProvider = new ItemProvider(this);
	private final Storage kitchenStorage = new Storage(this);
	private GlobalPos accessPointPos = null;
	private IActionHost actionHost = null;
	private IGrid grid = null;
	private MEStorage meStorage = null;

	private Mode mode = Mode.BIDIRECTIONAL;
	private int priority = 0;
	private ViewMode viewMode = ViewMode.NORMAL;

	// per network handler
	private IGrid registeredGrid = null;

	public StationBlockEntity(BlockPos pos, BlockState state) {
		super(Bridge.KITCHEN_STATION_BE.get(), pos, state);
	}

	@Override
	public List<BalmProvider<?>> getProviders() {
		return Lists.newArrayList(new BalmProvider<>(IKitchenItemProvider.class, itemProvider));
	}

	public Mode getMode() {
		return mode;
	}

	public int getPriority() {
		return priority;
	}

	public void setMode(Mode newMode) {
		if (newMode == null || newMode == this.mode) {
			return;
		}
		this.mode = newMode;
		setChanged();
		refreshStorageRegistration();
	}

	public void setPriority(int newPriority) {
		if (newPriority == this.priority) {
			return;
		}
		this.priority = newPriority;
		setChanged();
		refreshStorageRegistration();
	}

	public ViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(ViewMode newView) {
		if (newView == null || newView == this.viewMode) {
			return;
		}
		this.viewMode = newView;
		setChanged();
		if (level != null) {
			setConnected(grid != null);
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.culinarycompat.ae2_kitchen_station");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		return new StationMenu(containerId, playerInv, this);
	}

	@Override
	public void returnToMainMenu(Player player, ISubMenu subMenu) {
		if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
			net.minecraftforge.network.NetworkHooks.openScreen(sp, this, buf -> buf.writeBlockPos(getBlockPos()));
		}
	}

	@Override
	public ItemStack getMainMenuIcon() {
		return new ItemStack(Bridge.KITCHEN_STATION_ITEM.get());
	}

	public void openPriorityMenu(Player player) {
		if (!(player.level() instanceof ServerLevel)) {
			return;
		}
		MenuOpener.open(appeng.menu.implementations.PriorityMenu.TYPE, player, MenuLocators.forBlockEntity(this));
	}

	@Override
	public void mountInventories(IStorageMounts mounts) {
		if (mode.allowsCFBToAE()) {
			mounts.mount(kitchenStorage, priority);
		}
	}

	public void setConnected(boolean connected) {
		// OFF view forces a black screen regardless of grid state
		boolean displayConnected = viewMode != ViewMode.OFF && connected;
		level.blockEvent(worldPosition, Bridge.KITCHEN_STATION.get(), 0, 0);
		BlockState state = level.getBlockState(worldPosition);
		if (state.getValue(StationBlock.CONNECTED) != displayConnected) {
			level.setBlockAndUpdate(worldPosition, state.setValue(StationBlock.CONNECTED, displayConnected));
		}
		setChanged();
	}

	public void applyDataFromItemToBlockEntity(ItemStack stack) {
		var tag = stack.getTag();
		if (tag != null && tag.contains(StationItem.TAG_ACCESS_POINT_POS, Tag.TAG_COMPOUND)) {
			accessPointPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(StationItem.TAG_ACCESS_POINT_POS)).result()
					.map(Pair::getFirst).orElse(null);
			setNetworkProperties();
		} else {
			accessPointPos = null;
		}
		setChanged();
	}

	public void applyDataFromBlockEntityToItem(ItemStack stack) {
		if (accessPointPos != null) {
			GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, accessPointPos).result()
					.ifPresent(tagValue -> stack.getOrCreateTag().put(StationItem.TAG_ACCESS_POINT_POS, tagValue));
		}
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (accessPointPos != null) {
			GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, accessPointPos).result()
					.ifPresent(tagValue -> tag.put(StationItem.TAG_ACCESS_POINT_POS, tagValue));
		}
		tag.putString(NBT_MODE, mode.name());
		tag.putInt(NBT_PRIORITY, priority);
		tag.putString(NBT_VIEW, viewMode.name());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains(StationItem.TAG_ACCESS_POINT_POS)) {
			accessPointPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(StationItem.TAG_ACCESS_POINT_POS)).result()
					.map(Pair::getFirst).orElse(null);
		}
		if (tag.contains(NBT_MODE, Tag.TAG_STRING)) {
			mode = Mode.fromName(tag.getString(NBT_MODE));
		}
		if (tag.contains(NBT_PRIORITY, Tag.TAG_INT)) {
			priority = tag.getInt(NBT_PRIORITY);
		}
		if (tag.contains(NBT_VIEW, Tag.TAG_STRING)) {
			viewMode = ViewMode.fromName(tag.getString(NBT_VIEW));
		}
	}

	public MEStorage getNetworkStorage() {
		return meStorage;
	}

	public String getAccessPointPos() {
		if (actionHost != null && accessPointPos != null) {
			return accessPointPos.pos().getX() + ", " + accessPointPos.pos().getY() + ", "
					+ accessPointPos.pos().getZ();
		}
		return "";
	}

	public IActionHost getActionHost() {
		return actionHost;
	}

	@Nullable
	public void setNetworkProperties() {
		actionHost = null;
		grid = null;
		meStorage = null;

		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (accessPointPos == null) {
			return;
		}

		var linkedLevel = serverLevel.getServer().getLevel(accessPointPos.dimension());
		if (linkedLevel == null) {
			return;
		}

		var accessPointBlockEntity = Platform.getTickingBlockEntity(linkedLevel, accessPointPos.pos());
		if (!(accessPointBlockEntity instanceof IWirelessAccessPoint accessPoint)) {
			return;
		}

		actionHost = accessPoint;
		if (accessPoint.isActive()) {
			grid = accessPoint.getGrid();
			if (grid != null) {
				meStorage = grid.getStorageService().getInventory();
			}
		}
	}

	// register/unregister with the grid's storage service whenever our linked grid
	// changes
	private void updateStorageRegistration() {
		if (registeredGrid == grid) {
			return;
		}
		if (registeredGrid != null) {
			try {
				registeredGrid.getStorageService().removeGlobalStorageProvider(this);
			} catch (Exception ignored) {
			}
		}
		registeredGrid = grid;
		if (registeredGrid != null) {
			registeredGrid.getStorageService().addGlobalStorageProvider(this);
		}
	}

	private void refreshStorageRegistration() {
		if (registeredGrid != null) {
			registeredGrid.getStorageService().refreshGlobalStorageProvider(this);
		}
	}

	@Override
	public void setRemoved() {
		if (registeredGrid != null) {
			try {
				registeredGrid.getStorageService().removeGlobalStorageProvider(this);
			} catch (Exception ignored) {
			}
			registeredGrid = null;
		}
		super.setRemoved();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, StationBlockEntity blockEntity) {
		blockEntity.serverTick();
	}

	public void serverTick() {
		setNetworkProperties();
		updateStorageRegistration();
		setConnected(grid != null);
	}

	// preserves the access-point link NBT through pickup
	public static class LootFunction extends LootItemConditionalFunction {

		public LootFunction(LootItemCondition[] conditions) {
			super(conditions);
		}

		@Override
		protected ItemStack run(ItemStack stack, LootContext lootContext) {
			BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if (blockEntity instanceof StationBlockEntity be) {
				be.applyDataFromBlockEntityToItem(stack);
			}
			return stack;
		}

		@Override
		public LootItemFunctionType getType() {
			return LootFunctions.AE2_KITCHEN_STATION.get();
		}

		public static class Serializer extends LootItemConditionalFunction.Serializer<LootFunction> {
			@Override
			public LootFunction deserialize(JsonObject object, JsonDeserializationContext ctx,
					LootItemCondition[] conditions) {
				return new LootFunction(conditions);
			}
		}
	}
}
