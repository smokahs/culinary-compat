package com.hoshihoku.culinarycompat.mixin.cfb;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vectorwing.farmersdelight.common.block.CuttingBoardBlock;

@Mixin(value = CuttingBoardBlock.class, remap = false)
public abstract class CuttingBoardBlockMixin {
	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true, remap = true)
	private void culinarycompat$allowOnCfbCounter(BlockState state, LevelReader level, BlockPos pos,
			CallbackInfoReturnable<Boolean> cir) {
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (id != null && "cookingforblockheads".equals(id.getNamespace()) && "counter".equals(id.getPath())) {
			cir.setReturnValue(true);
		}
	}
}
