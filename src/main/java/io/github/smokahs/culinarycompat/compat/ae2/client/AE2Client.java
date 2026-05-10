package io.github.smokahs.culinarycompat.compat.ae2.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import io.github.smokahs.culinarycompat.CulinaryCompat;
import io.github.smokahs.culinarycompat.compat.ae2.Bridge;

// client-only registration for the ME station menu screen
@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AE2Client {

	private AE2Client() {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		if (!Bridge.isActive()) {
			return;
		}
		event.enqueueWork(() -> MenuScreens.register(Bridge.KITCHEN_STATION_MENU.get(), StationScreen::new));
	}
}
