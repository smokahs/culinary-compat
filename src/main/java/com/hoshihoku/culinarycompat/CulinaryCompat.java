package com.hoshihoku.culinarycompat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.hoshihoku.culinarycompat.compat.DepCheck;
import com.hoshihoku.culinarycompat.compat.cfb.CfbIntegration;
import com.hoshihoku.culinarycompat.config.ClientConfig;
import com.hoshihoku.culinarycompat.config.CommonConfig;
import com.hoshihoku.culinarycompat.foodnerf.FoodNerfHandler;
import com.hoshihoku.culinarycompat.network.NetworkHandler;
import com.hoshihoku.culinarycompat.registry.ModBlocks;
import com.hoshihoku.culinarycompat.registry.ModItems;
import com.hoshihoku.culinarycompat.registry.ModSounds;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

@Mod(CulinaryCompat.MODID)
public class CulinaryCompat {
	public static final String MODID = "culinarycompat";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CulinaryCompat(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();

		ModItems.ITEMS.register(modEventBus);
		ModBlocks.BLOCKS.register(modEventBus);
		ModSounds.SOUNDS.register(modEventBus);

		modEventBus.addListener(DepCheck::onCommonSetup);
		modEventBus.addListener(FoodNerfHandler::onCommonSetup);
		modEventBus.addListener(CulinaryCompat::onCommonSetup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

		CfbIntegration.init(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		NetworkHandler.register();
	}
}
