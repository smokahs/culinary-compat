package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public final class CfbMissingToolsTooltip {
	private static final String CFB_MISSING_TOOLS_KEY = "tooltip.cookingforblockheads:missing_tools";

	private CfbMissingToolsTooltip() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onItemTooltip(ItemTooltipEvent event) {
		List<Component> tooltip = event.getToolTip();
		int idx = -1;
		for (int i = 0; i < tooltip.size(); i++) {
			Component line = tooltip.get(i);
			if (line.getContents() instanceof TranslatableContents tc && CFB_MISSING_TOOLS_KEY.equals(tc.getKey())) {
				idx = i;
				break;
			}
		}
		if (idx < 0)
			return;

		ItemStack stack = event.getItemStack();
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
		if (id == null)
			return;

		List<String> tools = new ArrayList<>();
		Set<ResourceLocation> skillet = PamSkilletBridge.getExclusiveBridgeKeys();
		Set<ResourceLocation> pamPot = PamPotBridge.getExclusiveBridgeKeys();
		Set<ResourceLocation> fdPot = FdPotBridge.getExclusiveBridgeKeys();
		Set<ResourceLocation> bakeware = PamBakewareBridge.getExclusiveBridgeKeys();
		if (skillet.contains(id))
			tools.add("Skillet");
		if (pamPot.contains(id) || fdPot.contains(id))
			tools.add("Cooking Pot");
		if (bakeware.contains(id))
			tools.add("Bakeware");

		if (tools.isEmpty())
			return;

		MutableComponent replacement = Component.translatable(CFB_MISSING_TOOLS_KEY)
				.append(Component.literal(": " + String.join(", ", tools))).withStyle(ChatFormatting.RED);
		tooltip.set(idx, replacement);
	}
}
