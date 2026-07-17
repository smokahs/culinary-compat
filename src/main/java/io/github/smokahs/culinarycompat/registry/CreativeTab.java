package io.github.smokahs.culinarycompat.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import io.github.smokahs.culinarycompat.CulinaryCompat;
import io.github.smokahs.culinarycompat.compat.ae2.Bridge;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CreativeTab {
	private static final ResourceLocation CROPTOPIA_KNIFE = new ResourceLocation("croptopia", "knife");

	private CreativeTab() {
	}

	@SubscribeEvent
	public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			// bakeware serves Pam + croptopia baked goods; hide the block when both absent
			if (ModList.get().isLoaded("pamhc2foodcore") || ModList.get().isLoaded("croptopia")) {
				event.accept(Items.BAKEWARE.get());
			}
			event.accept(Items.GUIDEBOOK.get());
			if (Bridge.isActive()) {
				event.accept(Bridge.KITCHEN_STATION_ITEM.get());
			}
		}
		// FD present: nuke the redundant croptopia knife from every tab (incl. search)
		if (ModList.get().isLoaded("croptopia") && ModList.get().isLoaded("farmersdelight")) {
			removeCroptopiaKnife(event);
		}
	}

	private static void removeCroptopiaKnife(BuildCreativeModeTabContentsEvent event) {
		Item knife = ForgeRegistries.ITEMS.getValue(CROPTOPIA_KNIFE);
		if (knife == null) {
			return;
		}
		List<ItemStack> toRemove = new ArrayList<>();
		for (var entry : event.getEntries()) {
			if (entry.getKey().getItem() == knife) {
				toRemove.add(entry.getKey());
			}
		}
		for (ItemStack stack : toRemove) {
			event.getEntries().remove(stack);
		}
	}
}
