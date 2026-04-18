package com.hoshihoku.culinarycompat.compat.cfb.bake;

import java.util.List;

import net.blay09.mods.cookingforblockheads.client.gui.screen.RecipeBookScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.compat.cfb.PamBakewareBridge;
import com.hoshihoku.culinarycompat.config.CommonConfig;
import com.hoshihoku.culinarycompat.network.BakePhase;

@OnlyIn(Dist.CLIENT)
public final class BakeTooltipHandler {
	private static final String CFB_CLICK_CRAFT_ONE = "tooltip.cookingforblockheads:click_to_craft_one";
	private static final String CFB_CLICK_CRAFT_STACK = "tooltip.cookingforblockheads:click_to_craft_stack";

	private BakeTooltipHandler() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onItemTooltip(ItemTooltipEvent event) {
		if (!(Minecraft.getInstance().screen instanceof RecipeBookScreen))
			return;

		ItemStack stack = event.getItemStack();
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
		if (id == null)
			return;

		boolean isBakeware = PamBakewareBridge.getExclusiveBridgeKeys().contains(id);
		if (isBakeware && CommonConfig.bakewareEnabled) {
			List<Component> tip = event.getToolTip();
			tip.removeIf(line -> line.getContents() instanceof TranslatableContents tc
					&& (CFB_CLICK_CRAFT_ONE.equals(tc.getKey()) || CFB_CLICK_CRAFT_STACK.equals(tc.getKey())));
		}

		BakePhase phase = ClientBakeState.getPhase();
		if (phase == null)
			return;
		ResourceLocation pendingId = ClientBakeState.getOutputId();
		if (pendingId == null || !pendingId.equals(id))
			return;

		if (phase == BakePhase.CONFIRM) {
			event.getToolTip().add(Component.literal("Click again to bake!").withStyle(ChatFormatting.GOLD));
		} else if (phase == BakePhase.BAKING) {
			event.getToolTip().add(Component.literal("Baking\u2026").withStyle(ChatFormatting.GOLD));
		}
	}
}
