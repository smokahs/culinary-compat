package com.hoshihoku.culinarycompat.mixin.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderDispatcher.class, remap = false)
public abstract class FdCookwareModelOffsetMixin {
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$FD_SKILLET = new ResourceLocation("farmersdelight", "skillet");
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$FD_COOKING_POT = new ResourceLocation("farmersdelight",
			"cooking_pot");
	@Unique
	private static final String CULINARYCOMPAT$CFB_NAMESPACE = "cookingforblockheads";
	@Unique
	private static final float CULINARYCOMPAT$OFFSET = -1.0f / 16.0f;

	@Inject(method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"), remap = false, require = 0)
	private void culinarycompat$offsetCookware(BlockState state, BlockPos pos, BlockAndTintGetter level,
			PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, ModelData modelData,
			RenderType renderType, CallbackInfo ci) {
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		if (!CULINARYCOMPAT$FD_SKILLET.equals(id) && !CULINARYCOMPAT$FD_COOKING_POT.equals(id))
			return;
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation belowId = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (belowId == null || !CULINARYCOMPAT$CFB_NAMESPACE.equals(belowId.getNamespace()))
			return;
		poseStack.translate(0.0f, CULINARYCOMPAT$OFFSET, 0.0f);
	}
}
