package com.hoshihoku.culinarycompat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import com.hoshihoku.culinarycompat.CulinaryCompat;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.BooleanValue NERF_PAMS;
	private static final ForgeConfigSpec.BooleanValue INEDIBLE_INGREDIENTS;
	private static final ForgeConfigSpec.BooleanValue NERF_MINECRAFT;

	private static final ForgeConfigSpec.BooleanValue BAKEWARE_ENABLED;
	private static final ForgeConfigSpec.IntValue BAKEWARE_DURATION_TICKS;
	private static final ForgeConfigSpec.BooleanValue BAKEWARE_REFUND_ON_CANCEL;

	static {
		BUILDER.comment("Pam's HarvestCraft 2 food rebalancing. Requires restart to take effect.").push("foodNerf");
		NERF_PAMS = BUILDER
				.comment("rebalance Pam's HarvestCraft 2 food values to 1.12.2 baseline (Thanks Pixel1011!).")
				.translation("culinarycompat.config.nerfpams").define("nerfPams", true);
		INEDIBLE_INGREDIENTS = BUILDER.comment(
				"strip food properties from ingredient items (butter, spices, fresh water, etc.) so they cannot be eaten.")
				.translation("culinarycompat.config.inedibleingredients").define("inedibleIngredients", true);
		NERF_MINECRAFT = BUILDER.comment("Rebalance vanilla Minecraft food values to Pam's 1.12.2 baseline.")
				.translation("culinarycompat.config.nerfminecraft").define("nerfMinecraft", false);
		BUILDER.pop();

		BUILDER.comment("Bakeware cooking flow in the CFB cooking table.").push("bakeware");
		BAKEWARE_ENABLED = BUILDER
				.comment("enable use cooldown on bakeware recipes. when false, bakeware recipes craft instantly.")
				.translation("culinarycompat.config.bakewareEnabled").define("enabled", true);
		BAKEWARE_DURATION_TICKS = BUILDER.comment("bake duration in ticks.")
				.translation("culinarycompat.config.bakewareDurationTicks").defineInRange("durationTicks", 80, 0, 600);
		BAKEWARE_REFUND_ON_CANCEL = BUILDER
				.comment("refund ingredients if the player closes the cooking table menu while a bake is in progress.")
				.translation("culinarycompat.config.bakewareRefundOnCancel").define("refundOnCancel", true);
		BUILDER.pop();
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public static boolean nerfPams;
	public static boolean inedibleIngredients;
	public static boolean nerfMinecraft;

	public static boolean bakewareEnabled;
	public static int bakewareDurationTicks;
	public static boolean bakewareRefundOnCancel;

	private CommonConfig() {
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		if (event.getConfig().getSpec() != SPEC)
			return;
		nerfPams = NERF_PAMS.get();
		inedibleIngredients = INEDIBLE_INGREDIENTS.get();
		nerfMinecraft = NERF_MINECRAFT.get();
		bakewareEnabled = BAKEWARE_ENABLED.get();
		bakewareDurationTicks = BAKEWARE_DURATION_TICKS.get();
		bakewareRefundOnCancel = BAKEWARE_REFUND_ON_CANCEL.get();
	}
}
