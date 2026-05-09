package com.hoshihoku.culinarycompat.mixin.cfb;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vectorwing.farmersdelight.common.block.CuttingBoardBlock;

@Mixin(value = CuttingBoardBlock.class, remap = false)
public abstract class CuttingBoard {
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$CFB_COUNTER = new ResourceLocation("cookingforblockheads",
			"counter");
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$CFB_CORNER = new ResourceLocation("cookingforblockheads",
			"corner");
	@Unique
	private static final VoxelShape CULINARYCOMPAT$OFFSET_SHAPE = Block.box(1.0D, -1.0D, 1.0D, 15.0D, 0.0D, 15.0D);

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true, remap = true)
	private void culinarycompat$allowOnCfbCounter(BlockState state, LevelReader level, BlockPos pos,
			CallbackInfoReturnable<Boolean> cir) {
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (CULINARYCOMPAT$CFB_COUNTER.equals(id) || CULINARYCOMPAT$CFB_CORNER.equals(id)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true, remap = true, require = 0)
	private void culinarycompat$offsetShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
			CallbackInfoReturnable<VoxelShape> cir) {
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation belowId = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (CULINARYCOMPAT$CFB_COUNTER.equals(belowId) || CULINARYCOMPAT$CFB_CORNER.equals(belowId)) {
			cir.setReturnValue(CULINARYCOMPAT$OFFSET_SHAPE);
		}
	}
}
