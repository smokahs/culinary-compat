package com.hoshihoku.culinarycompat.compat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import com.hoshihoku.culinarycompat.CulinaryCompat;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID)
public final class DepCheck {
	private static final String[] DEPS = {"farmersdelight", "pamhc2foodcore", "pamhc2crops", "pamhc2trees",
			"pamhc2foodextended"};

	private static List<String> missing = List.of();

	private DepCheck() {
	}

	public static void onCommonSetup(FMLCommonSetupEvent event) {
		List<String> found = new ArrayList<>();
		for (String id : DEPS) {
			if (!ModList.get().isLoaded(id))
				found.add(id);
		}
		missing = List.copyOf(found);
		if (!missing.isEmpty()) {
			CulinaryCompat.LOGGER.warn("Culinary Compat running without {}. Install for all compat recipes/tags.",
					String.join(", ", missing));
		}
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (missing.isEmpty())
			return;
		Component msg = Component.literal("[Culinary Compat] Missing optional deps: " + String.join(", ", missing)
				+ ". Install for full recipe/tag integration.").withStyle(ChatFormatting.GOLD);
		event.getEntity().sendSystemMessage(msg);
	}
}
