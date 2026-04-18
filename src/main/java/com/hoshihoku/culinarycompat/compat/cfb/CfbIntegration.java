package com.hoshihoku.culinarycompat.compat.cfb;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.compat.cfb.bake.BakeState;

public final class CfbIntegration {
	public static final String CFB_MODID = "cookingforblockheads";
	public static final String FD_MODID = "farmersdelight";

	private static boolean active = false;

	private CfbIntegration() {
	}

	public static boolean isActive() {
		return active;
	}

	public static void init(IEventBus modEventBus) {
		if (!ModList.get().isLoaded(CFB_MODID) || !ModList.get().isLoaded(FD_MODID)) {
			return;
		}
		active = true;
		CulinaryCompat.LOGGER.info("CFB + FD detected — enabling cutting board kitchen integration.");

		MinecraftForge.EVENT_BUS.register(CfbKitchenConnector.class);
		MinecraftForge.EVENT_BUS.register(CfbTooltip.class);
		MinecraftForge.EVENT_BUS.register(CfbMissingToolsTooltip.class);
		MinecraftForge.EVENT_BUS.register(FdBridges.Cutting.class);
		MinecraftForge.EVENT_BUS.register(CfbCraftSound.class);
		MinecraftForge.EVENT_BUS.register(BakeState.Manager.class);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			MinecraftForge.EVENT_BUS.register(BakeState.Tooltip.class);
		}

		if (ModList.get().isLoaded(PamCuttingBridge.PAM_MODID)) {
			CulinaryCompat.LOGGER
					.info("Pam food core detected — enabling Pam cuttingboarditem → FD knife replacement.");
			modEventBus.register(PamCreativeTabFilter.class);
		}
	}
}
