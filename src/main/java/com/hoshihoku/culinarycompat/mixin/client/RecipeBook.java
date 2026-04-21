package com.hoshihoku.culinarycompat.mixin.client;

import net.blay09.mods.cookingforblockheads.api.FoodRecipeWithStatus;
import net.blay09.mods.cookingforblockheads.api.RecipeStatus;
import net.blay09.mods.cookingforblockheads.client.gui.screen.RecipeBookScreen;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.blay09.mods.cookingforblockheads.menu.slot.RecipeFakeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeBookScreen.class, remap = false)
public abstract class RecipeBook extends AbstractContainerScreen<RecipeBookMenu> {

	public RecipeBook(RecipeBookMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}

	@Inject(method = "render", at = @At("RETURN"), remap = true, require = 0)
	private void culinarycompat$greyMissingTools(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks,
			CallbackInfo ci) {
		var pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(0, 0, 300);
		for (Slot slot : this.menu.slots) {
			if (slot instanceof RecipeFakeSlot rfs) {
				FoodRecipeWithStatus recipe = rfs.getRecipe();
				if (recipe != null && recipe.getStatus() == RecipeStatus.MISSING_TOOLS) {
					int x = this.leftPos + slot.x;
					int y = this.topPos + slot.y;
					guiGraphics.fillGradient(x, y, x + 16, y + 16, 0xAA1A1A1A, 0xAA0A0A0A);
				}
			}
		}
		pose.popPose();
	}
}
