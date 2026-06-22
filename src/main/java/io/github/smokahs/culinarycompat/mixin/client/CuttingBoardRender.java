package io.github.smokahs.culinarycompat.mixin.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.smokahs.culinarycompat.recipe.MultiCuttingExtras;
import vectorwing.farmersdelight.client.renderer.CuttingBoardRenderer;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

@Mixin(value = CuttingBoardRenderer.class, remap = false)
public abstract class CuttingBoardRender {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
	private void culinarycompat$multiRender(CuttingBoardBlockEntity be, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay, CallbackInfo ci) {
		List<ItemStack> items = new ArrayList<>();
		ItemStack base = be.getInventory().getStackInSlot(0);
		if (!base.isEmpty()) {
			items.add(base);
		}
		if (be instanceof MultiCuttingExtras provider) {
			IItemHandler extras = provider.culinarycompat$getExtras();
			for (int i = 0; i < extras.getSlots(); i++) {
				ItemStack s = extras.getStackInSlot(i);
				if (!s.isEmpty()) {
					items.add(s);
				}
			}
		}
		int n = items.size();
		if (n <= 1 || be.isItemCarvingBoard()) {
			return;
		}
		Direction facing = be.getBlockState().getValue(CuttingBoardBlock.FACING).getOpposite();
		int posLong = (int) be.getBlockPos().asLong();
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		for (int i = 0; i < n; i++) {
			ItemStack stack = items.get(i);
			// spiral each item out a little and give it its own yaw so blocks don't overlap
			// and z-fight
			double theta = i * 2.3999632;
			double radius = 0.05 * Math.sqrt(i);
			double ox = radius * Math.cos(theta);
			double oz = radius * Math.sin(theta);
			poseStack.pushPose();
			poseStack.translate(0.5 + ox, 0.08 + i * 0.04, 0.5 + oz);
			poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + i * 50.0f));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
			poseStack.scale(0.55f, 0.55f, 0.55f);
			itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack,
					buffer, be.getLevel(), posLong);
			poseStack.popPose();
		}
		ci.cancel();
	}
}
