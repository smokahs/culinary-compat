package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class PamCreativeTabFilter {
	private static final ResourceLocation[] HIDDEN_ITEMS = {PamCuttingBridge.cuttingboardItemId(),
			PamSkilletBridge.skilletItemId(), PamPotBridge.potItemId(),
			new ResourceLocation("pamhc2foodcore", "bakewareitem"),
			new ResourceLocation("pamhc2foodcore", "doughitem")};

	private PamCreativeTabFilter() {
	}

	@SubscribeEvent
	public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
		Set<Item> hidden = new HashSet<>();
		for (ResourceLocation id : HIDDEN_ITEMS) {
			Item it = ForgeRegistries.ITEMS.getValue(id);
			if (it != null)
				hidden.add(it);
		}
		if (hidden.isEmpty())
			return;

		List<ItemStack> toRemove = new ArrayList<>();
		for (java.util.Map.Entry<ItemStack, ?> entry : event.getEntries()) {
			if (hidden.contains(entry.getKey().getItem()))
				toRemove.add(entry.getKey());
		}
		for (ItemStack stack : toRemove) {
			event.getEntries().remove(stack);
		}
	}
}
