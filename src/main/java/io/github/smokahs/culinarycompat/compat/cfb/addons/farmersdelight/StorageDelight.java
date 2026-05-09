package io.github.smokahs.culinarycompat.compat.cfb.addons.farmersdelight;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

import io.github.smokahs.culinarycompat.CulinaryCompat;

public final class StorageDelight {
	public static final String SD_MODID = "storagedelight";

	private StorageDelight() {
	}

	public static void init() {
		if (!ModList.get().isLoaded(SD_MODID)) {
			return;
		}
		CulinaryCompat.LOGGER.info("Storage Delight detected, CFB integration active.");
		if (FMLEnvironment.dist == Dist.CLIENT) {
			MinecraftForge.EVENT_BUS.register(KitchenTooltip.class);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static final class KitchenTooltip {
		private static final String TOOLTIP_KEY = "tooltip.cookingforblockheads:multiblock_kitchen";

		private KitchenTooltip() {
		}

		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event) {
			ItemStack stack = event.getItemStack();
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (id == null || !SD_MODID.equals(id.getNamespace())) {
				return;
			}
			List<Component> tip = event.getToolTip();
			for (Component line : tip) {
				if (line.getContents() instanceof TranslatableContents tc && TOOLTIP_KEY.equals(tc.getKey())) {
					return;
				}
			}
			tip.add(Component.translatable(TOOLTIP_KEY).withStyle(ChatFormatting.YELLOW));
		}
	}
}
