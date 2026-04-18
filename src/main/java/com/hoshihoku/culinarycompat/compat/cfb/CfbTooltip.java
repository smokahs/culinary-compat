package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public final class CfbTooltip {
	private static final Set<ResourceLocation> KITCHEN_MEMBER_ITEMS = Set.of(
			new ResourceLocation(CfbIntegration.FD_MODID, "cutting_board"),
			new ResourceLocation(CfbIntegration.FD_MODID, "stove"),
			new ResourceLocation(CfbIntegration.FD_MODID, "skillet"),
			new ResourceLocation(CfbIntegration.FD_MODID, "cooking_pot"),
			new ResourceLocation("culinarycompat", "bakeware"));
	private static final ResourceLocation FD_NETHERITE_KNIFE = new ResourceLocation(CfbIntegration.FD_MODID,
			"netherite_knife");
	private static final String TOOLTIP_KEY = "tooltip.cookingforblockheads:multiblock_kitchen";

	private CfbTooltip() {
	}

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
		if (id == null)
			return;
		if (KITCHEN_MEMBER_ITEMS.contains(id)) {
			event.getToolTip().add(Component.translatable(TOOLTIP_KEY).withStyle(ChatFormatting.YELLOW));
		} else if (FD_NETHERITE_KNIFE.equals(id)) {
			event.getToolTip().add(
					Component.literal("Never dulls when used in a kitchen.").withStyle(ChatFormatting.DARK_PURPLE));
		}
	}
}
