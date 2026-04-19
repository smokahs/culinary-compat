package com.hoshihoku.culinarycompat.mixin.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vectorwing.farmersdelight.client.renderer.CuttingBoardRenderer;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

@Mixin(value = CuttingBoardRenderer.class, remap = false)
public abstract class CuttingBoardRendererOffsetMixin {
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$CFB_COUNTER = new ResourceLocation("cookingforblockheads",
			"counter");
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$CFB_CORNER = new ResourceLocation("cookingforblockheads",
			"corner");
	@Unique
	private static final float CULINARYCOMPAT$OFFSET = -1.0f / 16.0f;

	@Inject(method = "render(Lvectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), remap = false, require = 0)
	private void culinarycompat$offsetBer(CuttingBoardBlockEntity be, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
		Level level = be.getLevel();
		if (level == null)
			return;
		BlockPos pos = be.getBlockPos();
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation belowId = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (!CULINARYCOMPAT$CFB_COUNTER.equals(belowId) && !CULINARYCOMPAT$CFB_CORNER.equals(belowId))
			return;
		poseStack.translate(0.0f, CULINARYCOMPAT$OFFSET, 0.0f);
	}
}
