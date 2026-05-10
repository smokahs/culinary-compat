package io.github.smokahs.culinarycompat.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import io.github.smokahs.culinarycompat.CulinaryCompat;
import io.github.smokahs.culinarycompat.compat.ae2.Bridge;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CreativeTab {
	private CreativeTab() {
	}

	@SubscribeEvent
	public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(Items.BAKEWARE.get());
			event.accept(Items.GUIDEBOOK.get());
			if (Bridge.isActive()) {
				event.accept(Bridge.KITCHEN_STATION_ITEM.get());
			}
		}
	}
}
