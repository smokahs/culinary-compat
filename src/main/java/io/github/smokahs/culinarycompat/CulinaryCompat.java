package io.github.smokahs.culinarycompat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import io.github.smokahs.culinarycompat.compat.DepCheck;
import io.github.smokahs.culinarycompat.compat.ae2.Bridge;
import io.github.smokahs.culinarycompat.compat.cfb.CFB;
import io.github.smokahs.culinarycompat.config.Configs;
import io.github.smokahs.culinarycompat.foodnerf.FoodNerf;
import io.github.smokahs.culinarycompat.network.Network;
import io.github.smokahs.culinarycompat.registry.Blocks;
import io.github.smokahs.culinarycompat.registry.Items;
import io.github.smokahs.culinarycompat.registry.LootFunctions;
import io.github.smokahs.culinarycompat.registry.Recipes;
import io.github.smokahs.culinarycompat.registry.Sounds;
import io.github.smokahs.culinarycompat.resource.OverridePack;

@Mod(CulinaryCompat.MODID)
public class CulinaryCompat {
	public static final String MODID = "culinarycompat";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CulinaryCompat(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();

		Items.ITEMS.register(modEventBus);
		Blocks.BLOCKS.register(modEventBus);
		Sounds.SOUNDS.register(modEventBus);
		Recipes.RECIPE_SERIALIZERS.register(modEventBus);
		Recipes.RECIPE_TYPES.register(modEventBus);
		LootFunctions.LOOT_FUNCTIONS.register(modEventBus);

		modEventBus.addListener(DepCheck::onCommonSetup);
		modEventBus.addListener(FoodNerf::onCommonSetup);
		modEventBus.addListener(CulinaryCompat::onCommonSetup);
		modEventBus.addListener(OverridePack::onAddPackFinders);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.Common.SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configs.Client.SPEC);

		CFB.init(modEventBus);
		Bridge.init(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		Network.register();
	}
}
