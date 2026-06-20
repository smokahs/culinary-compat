package io.github.smokahs.culinarycompat.mixin.fd;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.smokahs.culinarycompat.config.Configs;
import io.github.smokahs.culinarycompat.recipe.MultiCutting;
import io.github.smokahs.culinarycompat.recipe.MultiCuttingExtras;
import io.github.smokahs.culinarycompat.registry.Recipes;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.registry.ModSounds;
import vectorwing.farmersdelight.common.utility.ItemUtils;

@Mixin(value = CuttingBoardBlockEntity.class, remap = false)
public abstract class CuttingBoardEntity implements MultiCuttingExtras {
	@Shadow
	private ItemStackHandler inventory;

	@Shadow
	private boolean isItemCarvingBoard;

	@Shadow
	public abstract void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch);

	@Unique
	private final ItemStackHandler culinarycompat$extras = new ItemStackHandler(MultiCutting.MAX_INPUTS - 1) {
		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}
		@Override
		protected void onContentsChanged(int slot) {
			((SyncedBlockEntityAccessor) (Object) CuttingBoardEntity.this).culinarycompat$invokeInventoryChanged();
		}
	};

	private static final TagKey<Item> CULINARYCOMPAT$KNIVES_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tools/knives"));
	private static final ResourceLocation CULINARYCOMPAT$NETHERITE_KNIFE_ID = new ResourceLocation("farmersdelight",
			"netherite_knife");

	@Unique
	private static boolean culinarycompat$isImmuneKnife(ItemStack stack) {
		if (!Configs.Common.opNetheriteKnife) {
			return false;
		}
		ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
		return CULINARYCOMPAT$NETHERITE_KNIFE_ID.equals(id);
	}

	// FD 1.3.1 dropped the slot limit on its handler, so it messed up the multi
	// ingredient recipes
	// FD's addItem returns boolean (true if stored); CIR must match or mixin crashes casting to Boolean
	@Inject(method = "addItem", at = @At("HEAD"), cancellable = true)
	private void culinarycompat$addOne(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (itemStack.isEmpty() || isItemCarvingBoard) {
			return;
		}
		if (inventory.getStackInSlot(0).isEmpty()) {
			inventory.setStackInSlot(0, itemStack.split(1));
			((SyncedBlockEntityAccessor) (Object) this).culinarycompat$invokeInventoryChanged();
			cir.setReturnValue(true);
			return;
		}
		for (int i = 0; i < culinarycompat$extras.getSlots(); i++) {
			if (culinarycompat$extras.getStackInSlot(i).isEmpty()) {
				culinarycompat$extras.setStackInSlot(i, itemStack.split(1));
				cir.setReturnValue(true);
				return;
			}
		}
		cir.setReturnValue(false);
	}

	@Inject(method = "removeItem", at = @At("HEAD"), cancellable = true)
	private void culinarycompat$removeLast(CallbackInfoReturnable<ItemStack> cir) {
		for (int i = culinarycompat$extras.getSlots() - 1; i >= 0; i--) {
			ItemStack s = culinarycompat$extras.getStackInSlot(i);
			if (!s.isEmpty()) {
				ItemStack out = s.split(1);
				culinarycompat$extras.setStackInSlot(i, ItemStack.EMPTY);
				cir.setReturnValue(out);
				return;
			}
		}
		ItemStack base = inventory.getStackInSlot(0);
		if (!base.isEmpty()) {
			ItemStack out = base.split(1);
			if (base.isEmpty()) {
				inventory.setStackInSlot(0, ItemStack.EMPTY);
				isItemCarvingBoard = false;
			}
			((SyncedBlockEntityAccessor) (Object) this).culinarycompat$invokeInventoryChanged();
			cir.setReturnValue(out);
			return;
		}
		cir.setReturnValue(ItemStack.EMPTY);
	}

	@Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
	private void culinarycompat$isEmptyMulti(CallbackInfoReturnable<Boolean> cir) {
		if (!inventory.getStackInSlot(0).isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		for (int i = 0; i < culinarycompat$extras.getSlots(); i++) {
			if (!culinarycompat$extras.getStackInSlot(i).isEmpty()) {
				cir.setReturnValue(false);
				return;
			}
		}
		cir.setReturnValue(true);
	}

	@Inject(method = "processStoredItemUsingTool", at = @At("HEAD"), cancellable = true)
	private void culinarycompat$tryMultiCutting(ItemStack toolStack, @Nullable Player player,
			CallbackInfoReturnable<Boolean> cir) {
		BlockEntity selfBe = (BlockEntity) (Object) this;
		Level level = selfBe.getLevel();
		if (level == null || isItemCarvingBoard) {
			return;
		}
		if (!toolStack.is(CULINARYCOMPAT$KNIVES_TAG)) {
			return;
		}
		if (inventory.getStackInSlot(0).getCount() > 1) {
			return;
		}
		boolean hasExtras = false;
		for (int i = 0; i < culinarycompat$extras.getSlots(); i++) {
			if (!culinarycompat$extras.getStackInSlot(i).isEmpty()) {
				hasExtras = true;
				break;
			}
		}
		if (!hasExtras) {
			return;
		}
		if (level.isClientSide) {
			cir.setReturnValue(true);
			return;
		}
		NonNullList<ItemStack> all = NonNullList.create();
		all.add(inventory.getStackInSlot(0));
		for (int i = 0; i < culinarycompat$extras.getSlots(); i++) {
			ItemStack s = culinarycompat$extras.getStackInSlot(i);
			if (!s.isEmpty()) {
				all.add(s);
			}
		}
		RecipeWrapper wrapper = new RecipeWrapper(culinarycompat$wrapperFor(all));
		Optional<MultiCutting> match = level.getRecipeManager().getRecipeFor(Recipes.MULTI_CUTTING_TYPE.get(), wrapper,
				level);
		if (match.isEmpty()) {
			if (player != null) {
				player.displayClientMessage(net.minecraft.network.chat.Component.literal("That doesn't seem right..."),
						true);
			}
			cir.setReturnValue(false);
			return;
		}
		MultiCutting recipe = match.get();
		ItemStack result = recipe.assemble(wrapper, level.registryAccess());
		BlockPos pos = selfBe.getBlockPos();
		Direction dir = ((Direction) selfBe.getBlockState().getValue((Property<?>) CuttingBoardBlock.FACING))
				.getCounterClockWise();
		ItemUtils.spawnItemEntity(level, result.copy(), pos.getX() + 0.5 + dir.getStepX() * 0.2, pos.getY() + 0.2,
				pos.getZ() + 0.5 + dir.getStepZ() * 0.2, dir.getStepX() * 0.2, 0.0, dir.getStepZ() * 0.2);
		if (!culinarycompat$isImmuneKnife(toolStack)) {
			if (player != null) {
				toolStack.hurtAndBreak(1, (LivingEntity) player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
			} else if (toolStack.hurt(1, level.random, null)) {
				toolStack.setCount(0);
			}
		}
		playSound(ModSounds.BLOCK_CUTTING_BOARD_KNIFE.get(), 0.8f, 1.0f);
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		for (int i = 0; i < culinarycompat$extras.getSlots(); i++) {
			culinarycompat$extras.setStackInSlot(i, ItemStack.EMPTY);
		}
		((SyncedBlockEntityAccessor) (Object) this).culinarycompat$invokeInventoryChanged();
		cir.setReturnValue(true);
	}

	@Unique
	private static ItemStackHandler culinarycompat$wrapperFor(NonNullList<ItemStack> items) {
		ItemStackHandler h = new ItemStackHandler(items.size());
		for (int i = 0; i < items.size(); i++) {
			h.setStackInSlot(i, items.get(i));
		}
		return h;
	}

	@Override
	public ItemStackHandler culinarycompat$getExtras() {
		return culinarycompat$extras;
	}

	@Redirect(method = "lambda$processStoredItemUsingTool$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	private void culinarycompat$skipKnifeDamage(ItemStack stack, int amount, LivingEntity entity,
			java.util.function.Consumer<LivingEntity> onBroken) {
		if (culinarycompat$isImmuneKnife(stack)) {
			return;
		}
		stack.hurtAndBreak(amount, entity, onBroken);
	}

	@Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
	private void culinarycompat$saveExtras(CompoundTag tag, CallbackInfo ci) {
		tag.put("CulinaryCompatExtras", culinarycompat$extras.serializeNBT());
	}

	@Inject(method = "load", at = @At("TAIL"), remap = true)
	private void culinarycompat$loadExtras(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("CulinaryCompatExtras")) {
			culinarycompat$extras.deserializeNBT(tag.getCompound("CulinaryCompatExtras"));
		}
	}
}
