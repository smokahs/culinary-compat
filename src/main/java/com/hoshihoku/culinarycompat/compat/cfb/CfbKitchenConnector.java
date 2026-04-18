package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.function.BiPredicate;

import net.blay09.mods.cookingforblockheads.ForgeCookingForBlockheads;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenConnector;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CfbKitchenConnector {
	private static final ResourceLocation KEY = new ResourceLocation("culinarycompat", "kitchen_connector");

	private static final ResourceLocation FD_CUTTING_BOARD_BE = new ResourceLocation(CfbIntegration.FD_MODID,
			"cutting_board");
	private static final ResourceLocation FD_STOVE_BE = new ResourceLocation(CfbIntegration.FD_MODID, "stove");
	private static final ResourceLocation FD_SKILLET_BE = new ResourceLocation(CfbIntegration.FD_MODID, "skillet");
	private static final ResourceLocation FD_COOKING_POT_BE = new ResourceLocation(CfbIntegration.FD_MODID,
			"cooking_pot");

	private static final ResourceLocation CFB_COUNTER = new ResourceLocation(CfbIntegration.CFB_MODID, "counter");
	private static final ResourceLocation FD_STOVE_BLOCK = new ResourceLocation(CfbIntegration.FD_MODID, "stove");

	private CfbKitchenConnector() {
	}

	@SubscribeEvent
	public static void onAttach(AttachCapabilitiesEvent<BlockEntity> event) {
		BlockEntity be = event.getObject();
		ResourceLocation typeId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType());
		if (typeId == null)
			return;

		if (FD_CUTTING_BOARD_BE.equals(typeId)) {
			event.addCapability(KEY, new Provider(be, CfbKitchenConnector::hasCounterBelow));
		} else if (FD_STOVE_BE.equals(typeId)) {
			event.addCapability(KEY, new Provider(be, CfbKitchenConnector::hasCfbNeighborHorizontally));
		} else if (FD_SKILLET_BE.equals(typeId)) {
			event.addCapability(KEY, new Provider(be, CfbKitchenConnector::hasCookingSurfaceBelow));
		} else if (FD_COOKING_POT_BE.equals(typeId)) {
			event.addCapability(KEY, new Provider(be, CfbKitchenConnector::hasCookingSurfaceBelow));
		}
	}

	private static boolean hasCounterBelow(Level level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return CFB_COUNTER.equals(ForgeRegistries.BLOCKS.getKey(below.getBlock()));
	}

	private static boolean hasStoveBelow(Level level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return FD_STOVE_BLOCK.equals(ForgeRegistries.BLOCKS.getKey(below.getBlock()));
	}

	private static boolean hasCookingSurfaceBelow(Level level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (id == null)
			return false;
		if (FD_STOVE_BLOCK.equals(id))
			return true;
		return CfbIntegration.CFB_MODID.equals(id.getNamespace());
	}

	private static boolean hasCfbNeighborHorizontally(Level level, BlockPos pos) {
		for (Direction d : Direction.Plane.HORIZONTAL) {
			BlockState neighbor = level.getBlockState(pos.relative(d));
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(neighbor.getBlock());
			if (id != null && CfbIntegration.CFB_MODID.equals(id.getNamespace())) {
				return true;
			}
		}
		return false;
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
}
