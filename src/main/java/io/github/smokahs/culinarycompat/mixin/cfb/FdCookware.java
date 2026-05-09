package com.hoshihoku.culinarycompat.mixin.cfb;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.SkilletBlock;

@Mixin(value = {SkilletBlock.class, CookingPotBlock.class}, remap = false)
public abstract class FdCookware {
	@Unique
	private static final String CULINARYCOMPAT$CFB_NAMESPACE = "cookingforblockheads";
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$COOKING_TABLE = new ResourceLocation(
			CULINARYCOMPAT$CFB_NAMESPACE, "cooking_table");
	@Unique
	private static final double CULINARYCOMPAT$OFFSET = -1.0 / 16.0;

	@Inject(method = "getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"), cancellable = true, remap = true, require = 0)
	private void culinarycompat$offsetShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
			CallbackInfoReturnable<VoxelShape> cir) {
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation belowId = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (belowId != null && CULINARYCOMPAT$CFB_NAMESPACE.equals(belowId.getNamespace())
				&& !CULINARYCOMPAT$COOKING_TABLE.equals(belowId)) {
			cir.setReturnValue(cir.getReturnValue().move(0, CULINARYCOMPAT$OFFSET, 0));
		}
	}
}
