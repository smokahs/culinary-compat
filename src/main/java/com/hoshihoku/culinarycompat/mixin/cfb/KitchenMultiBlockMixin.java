package com.hoshihoku.culinarycompat.mixin.cfb;

import java.util.HashSet;
import java.util.Set;

import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.compat.cfb.CcKitchenMemberView;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KitchenMultiBlock.class, remap = false)
public abstract class KitchenMultiBlockMixin implements CcKitchenMemberView {
	@Unique
	private final Set<ResourceLocation> culinarycompat$memberBlocks = new HashSet<>();

	@Unique
	private static final ResourceLocation CULINARYCOMPAT$BAKEWARE = new ResourceLocation("culinarycompat", "bakeware");
	@Unique
	private static final Set<ResourceLocation> CULINARYCOMPAT$NEIGHBOR_TARGETS = Set.of(CULINARYCOMPAT$BAKEWARE,
			new ResourceLocation("farmersdelight", "cutting_board"), new ResourceLocation("farmersdelight", "skillet"),
			new ResourceLocation("farmersdelight", "cooking_pot"));

	@Inject(method = "findNeighbourKitchenBlocks", at = @At("HEAD"), remap = false)
	private void culinarycompat$recordMember(Level level, BlockPos pos, boolean extendedUpSearch, CallbackInfo ci) {
		BlockState state = level.getBlockState(pos);
		if (!state.isAir()) {
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
			if (id != null) {
				culinarycompat$memberBlocks.add(id);
			}
		}
		for (Direction d : Direction.values()) {
			BlockPos n = pos.relative(d);
			BlockState ns = level.getBlockState(n);
			if (ns.isAir())
				continue;
			ResourceLocation nid = ForgeRegistries.BLOCKS.getKey(ns.getBlock());
			if (CULINARYCOMPAT$NEIGHBOR_TARGETS.contains(nid)) {
				culinarycompat$memberBlocks.add(nid);
			}
		}
	}

	@Override
	public Set<ResourceLocation> culinarycompat$getMemberBlocks() {
		return culinarycompat$memberBlocks;
	}
}
