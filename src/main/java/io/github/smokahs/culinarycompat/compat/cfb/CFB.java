package io.github.smokahs.culinarycompat.compat.cfb;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import net.blay09.mods.cookingforblockheads.ForgeCookingForBlockheads;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenConnector;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenConnector;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.smokahs.culinarycompat.CulinaryCompat;
import io.github.smokahs.culinarycompat.compat.cfb.addons.farmersdelight.StorageDelight;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

public final class CFB {
	public static final String CFB_MODID = "cookingforblockheads";
	public static final String FD_MODID = "farmersdelight";

	private static boolean active = false;

	private CFB() {
	}

	public static boolean isActive() {
		return active;
	}

	public static void init(IEventBus modEventBus) {
		if (!ModList.get().isLoaded(CFB_MODID) || !ModList.get().isLoaded(FD_MODID)) {
			return;
		}
		active = true;
		CulinaryCompat.LOGGER.info("CFB + FD detected, enabling cutting board kitchen integration.");

		MinecraftForge.EVENT_BUS.register(KitchenConnector.class);
		MinecraftForge.EVENT_BUS.register(FarmersDelight.Cutting.class);
		MinecraftForge.EVENT_BUS.register(UI.CraftSound.class);
		MinecraftForge.EVENT_BUS.register(BakeState.Manager.class);
		MinecraftForge.EVENT_BUS.register(CuttingBoardInteract.class);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			// client-only tooltips (thanks MaveTheMaverick and RainbowMagicMarker)
			MinecraftForge.EVENT_BUS.register(UI.Tooltip.class);
			MinecraftForge.EVENT_BUS.register(UI.MissingToolsTooltip.class);
			// wait for balm client runtime to be up before registering tooltip handler
			net.blay09.mods.balm.api.client.BalmClient.onRuntimeAvailable(BakeState.Tooltip::register);
		}

		StorageDelight.init();

		if (ModList.get().isLoaded(Pam.PAM_MODID)) {
			CulinaryCompat.LOGGER.info("Pam food core detected, enabling Pam cuttingboarditem → FD knife replacement.");
			modEventBus.register(Pam.CreativeTabFilter.class);
			MinecraftForge.EVENT_BUS.register(Pam.RecipeStripper.class);
		}

		if (ModList.get().isLoaded(Croptopia.CROPTOPIA_MODID)) {
			CulinaryCompat.LOGGER.info("Croptopia detected, enabling Croptopia kitchen bridge.");
			MinecraftForge.EVENT_BUS.register(Croptopia.Stripper.class);
		}
	}

	public interface KitchenMemberView {
		Set<ResourceLocation> culinarycompat$getMemberBlocks();

		// kitchen-only item providers (cabinets, drawers, racks) — excludes the
		// player inventory that KitchenMultiBlock.getItemProviders would append
		List<IKitchenItemProvider> culinarycompat$getKitchenItemProviders();
	}

	public static final class KitchenConnector {
		private static final ResourceLocation KEY = new ResourceLocation("culinarycompat", "kitchen_connector");
		private static final ResourceLocation ITEM_PROVIDER_KEY = new ResourceLocation("culinarycompat",
				"kitchen_item_provider");

		private static final ResourceLocation CFB_COUNTER = new ResourceLocation(CFB_MODID, "counter");
		private static final TagKey<Block> FD_HEAT_SOURCES = TagKey.create(Registries.BLOCK,
				new ResourceLocation(FD_MODID, "heat_sources"));
		private static final TagKey<Block> FD_CABINETS = TagKey.create(Registries.BLOCK,
				new ResourceLocation(FD_MODID, "cabinets"));
		static final Set<ResourceLocation> ADDON_STOVE_IDS = Set.of(
				new ResourceLocation("endersdelight", "endstone_stove"),
				new ResourceLocation("ends_delight", "end_stove"),
				new ResourceLocation("nethersdelight", "blackstone_stove"),
				new ResourceLocation("twilightdelight", "maze_stove"));

		private KitchenConnector() {
		}
		private static final BiPredicate<Level, BlockPos> ALWAYS_ACTIVE = (level, pos) -> true;

		@SubscribeEvent
		public static void onAttach(AttachCapabilitiesEvent<BlockEntity> event) {
			BlockEntity be = event.getObject();
			if (be instanceof CuttingBoardBlockEntity) {
				event.addCapability(KEY, new Provider(be, KitchenConnector::hasCounterBelow));
				return;
			}
			if (be instanceof StoveBlockEntity) {
				event.addCapability(KEY, new Provider(be, ALWAYS_ACTIVE));
				return;
			}
			if (be instanceof SkilletBlockEntity) {
				event.addCapability(KEY, new Provider(be, KitchenConnector::hasCookingSurfaceBelow));
				return;
			}
			if (be instanceof CookingPotBlockEntity) {
				event.addCapability(KEY, new Provider(be, KitchenConnector::hasCookingSurfaceBelow));
				return;
			}
			ResourceLocation typeId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType());
			if (typeId != null && ADDON_STOVE_IDS.contains(typeId)) {
				event.addCapability(KEY, new Provider(be, ALWAYS_ACTIVE));
				return;
			}
			BlockState beState = be.getBlockState();
			if (beState.is(FD_CABINETS)) {
				event.addCapability(KEY, new Provider(be, ALWAYS_ACTIVE));
				if (be instanceof Container container) {
					event.addCapability(ITEM_PROVIDER_KEY, new ItemProvider(container));
				}
			}
		}

		private static boolean hasCounterBelow(Level level, BlockPos pos) {
			BlockState below = level.getBlockState(pos.below());
			return CFB_COUNTER.equals(ForgeRegistries.BLOCKS.getKey(below.getBlock()));
		}

		private static boolean hasCookingSurfaceBelow(Level level, BlockPos pos) {
			BlockState below = level.getBlockState(pos.below());
			if (below.is(FD_HEAT_SOURCES))
				return true;
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(below.getBlock());
			return id != null && CFB_MODID.equals(id.getNamespace());
		}

		private static final class Provider implements ICapabilityProvider {
			private final BlockEntity owner;
			private final BiPredicate<Level, BlockPos> validity;
			private final IKitchenConnector connector = new DefaultKitchenConnector();
			private final LazyOptional<IKitchenConnector> opt = LazyOptional.of(() -> connector);

			Provider(BlockEntity owner, BiPredicate<Level, BlockPos> validity) {
				this.owner = owner;
				this.validity = validity;
			}

			@Override
			public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
				if (cap != ForgeCookingForBlockheads.KITCHEN_CONNECTOR_CAPABILITY) {
					return LazyOptional.empty();
				}
				Level level = owner.getLevel();
				if (level == null)
					return LazyOptional.empty();
				if (!validity.test(level, owner.getBlockPos()))
					return LazyOptional.empty();
				return opt.cast();
			}
		}

		private static final class ItemProvider implements ICapabilityProvider {
			private final IKitchenItemProvider provider;
			private final LazyOptional<IKitchenItemProvider> opt;

			ItemProvider(Container container) {
				this.provider = new DefaultKitchenItemProvider(container);
				this.opt = LazyOptional.of(() -> provider);
			}

			@Override
			public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
				if (cap != ForgeCookingForBlockheads.KITCHEN_ITEM_PROVIDER_CAPABILITY) {
					return LazyOptional.empty();
				}
				return opt.cast();
			}
		}
	}
}
