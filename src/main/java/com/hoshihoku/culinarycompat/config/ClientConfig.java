package com.hoshihoku.culinarycompat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import com.hoshihoku.culinarycompat.CulinaryCompat;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.BooleanValue BAKE_DING_SOUND;
	private static final ForgeConfigSpec.DoubleValue DING_VOLUME;

	static {
		BUILDER.comment("Sound effects played during the bake flow.").push("sounds");
		BAKE_DING_SOUND = BUILDER.comment("play oven ding sound when a bakeware recipe finishes baking.")
				.translation("culinarycompat.config.bakedingsound").define("bakeDingSound", true);
		DING_VOLUME = BUILDER.comment("Volume multiplier for the finish ding sound (0.0 - 1.0).")
				.translation("culinarycompat.config.dingvolume").defineInRange("dingVolume", 1.0, 0.0, 1.0);
		BUILDER.pop();
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public static boolean bakeDingSound;
	public static float dingVolume;

	private ClientConfig() {
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		if (event.getConfig().getSpec() != SPEC)
			return;
		bakeDingSound = BAKE_DING_SOUND.get();
		dingVolume = DING_VOLUME.get().floatValue();
	}
}
