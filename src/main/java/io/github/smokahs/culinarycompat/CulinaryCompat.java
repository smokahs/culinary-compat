package com.hoshihoku.culinarycompat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.hoshihoku.culinarycompat.compat.DepCheck;
import com.hoshihoku.culinarycompat.compat.cfb.CFB;
import com.hoshihoku.culinarycompat.config.Configs;
import com.hoshihoku.culinarycompat.foodnerf.FoodNerf;
import com.hoshihoku.culinarycompat.network.Network;
import com.hoshihoku.culinarycompat.registry.Blocks;
import com.hoshihoku.culinarycompat.registry.Items;
import com.hoshihoku.culinarycompat.registry.Sounds;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

@Mod(CulinaryCompat.MODID)
public class CulinaryCompat {
	public static final String MODID = "culinarycompat";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CulinaryCompat(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();

		Items.ITEMS.register(modEventBus);
		Blocks.BLOCKS.register(modEventBus);
		Sounds.SOUNDS.register(modEventBus);

		modEventBus.addListener(DepCheck::onCommonSetup);
		modEventBus.addListener(FoodNerf::onCommonSetup);
		modEventBus.addListener(CulinaryCompat::onCommonSetup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.Common.SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configs.Client.SPEC);

		CFB.init(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		Network.register();
	}
}
