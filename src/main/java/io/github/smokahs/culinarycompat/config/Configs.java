package io.github.smokahs.culinarycompat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import io.github.smokahs.culinarycompat.CulinaryCompat;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Configs {
	private Configs() {
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		if (event.getConfig().getSpec() == Common.SPEC)
			Common.load();
		else if (event.getConfig().getSpec() == Client.SPEC)
			Client.load();
	}

	public static final class Common {
		private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

		private static final ForgeConfigSpec.BooleanValue NERF_PAMS;
		private static final ForgeConfigSpec.BooleanValue NERF_CROPTOPIA;
		private static final ForgeConfigSpec.BooleanValue INEDIBLE_INGREDIENTS;
		private static final ForgeConfigSpec.BooleanValue NERF_MINECRAFT;

		private static final ForgeConfigSpec.BooleanValue BAKEWARE_ENABLED;
		private static final ForgeConfigSpec.IntValue BAKEWARE_DURATION_TICKS;
		private static final ForgeConfigSpec.BooleanValue BAKEWARE_REFUND_ON_CANCEL;

		private static final ForgeConfigSpec.BooleanValue OP_NETHERITE_KNIFE;
		private static final ForgeConfigSpec.BooleanValue MULTI_CUTTING;

		private static final ForgeConfigSpec.BooleanValue NOTIFY_MISSING_DEPS;

		static {
			BUILDER.comment("Food rebalancing (Pam's HarvestCraft 2, Croptopia). Requires restart to take effect.")
					.push("foodNerf");
			NERF_PAMS = BUILDER
					.comment("rebalance Pam's HarvestCraft 2 food values to 1.12.2 baseline (Thanks Pixel1011!).")
					.translation("culinarycompat.config.nerfpams").define("nerfPams", false);
			NERF_CROPTOPIA = BUILDER.comment(
					"rebalance Croptopia food values to Farmer's Delight saturation levels (sat 0.3 / 0.4 raw + hearty meals, nutrition capped at 14).")
					.translation("culinarycompat.config.nerfcroptopia").define("nerfCroptopia", true);
			INEDIBLE_INGREDIENTS = BUILDER.comment(
					"strip food properties from ingredient items (butter, spices, fresh water, etc.) so they cannot be eaten.")
					.translation("culinarycompat.config.inedibleingredients").define("inedibleIngredients", true);
			NERF_MINECRAFT = BUILDER.comment("Rebalance vanilla Minecraft food values to Pam's 1.12.2 baseline.")
					.translation("culinarycompat.config.nerfminecraft").define("nerfMinecraft", false);
			BUILDER.pop();

			BUILDER.comment("Bakeware cooking in the CFB cooking table.").push("bakeware");
			BAKEWARE_ENABLED = BUILDER
					.comment("enable use cooldown on bakeware recipes. when false, bakeware recipes craft instantly.")
					.translation("culinarycompat.config.bakewareEnabled").define("enabled", true);
			BAKEWARE_DURATION_TICKS = BUILDER.comment("bake duration in ticks.")
					.translation("culinarycompat.config.bakewareDurationTicks")
					.defineInRange("durationTicks", 80, 0, 600);
			BAKEWARE_REFUND_ON_CANCEL = BUILDER.comment(
					"refund ingredients if the player closes the cooking table menu while a bake is in progress.")
					.translation("culinarycompat.config.bakewareRefundOnCancel").define("refundOnCancel", true);
			BUILDER.pop();

			BUILDER.comment("Cutting board behavior.").push("cuttingBoard");
			OP_NETHERITE_KNIFE = BUILDER.comment(
					"OP Netherite knife! Farmer's Delight netherite knife takes no durability damage when used on the cutting board.")
					.translation("culinarycompat.config.opNetheriteKnife").define("opNetheriteKnife", true);
			MULTI_CUTTING = BUILDER.comment(
					"allow stacking extra items onto an occupied cutting board for multi-item cutting recipes and display.",
					"when false, only vanilla Farmer's Delight single-item cutting board behavior applies.")
					.translation("culinarycompat.config.multiCutting").define("multiCutting", true);
			BUILDER.pop();

			BUILDER.comment("Chat notifications.").push("notifications");
			NOTIFY_MISSING_DEPS = BUILDER
					.comment("show a chat message on login when certain optional dependencies are missing.")
					.translation("culinarycompat.config.notifyMissingDeps").define("notifyMissingDeps", false);
			BUILDER.pop();
		}

		public static final ForgeConfigSpec SPEC = BUILDER.build();

		public static boolean nerfPams;
		public static boolean nerfCroptopia;
		public static boolean inedibleIngredients;
		public static boolean nerfMinecraft;

		public static boolean bakewareEnabled;
		public static int bakewareDurationTicks;
		public static boolean bakewareRefundOnCancel;

		public static boolean opNetheriteKnife;
		public static boolean multiCutting;

		public static boolean notifyMissingDeps;

		private Common() {
		}

		static void load() {
			nerfPams = NERF_PAMS.get();
			nerfCroptopia = NERF_CROPTOPIA.get();
			inedibleIngredients = INEDIBLE_INGREDIENTS.get();
			nerfMinecraft = NERF_MINECRAFT.get();
			bakewareEnabled = BAKEWARE_ENABLED.get();
			bakewareDurationTicks = BAKEWARE_DURATION_TICKS.get();
			bakewareRefundOnCancel = BAKEWARE_REFUND_ON_CANCEL.get();
			opNetheriteKnife = OP_NETHERITE_KNIFE.get();
			multiCutting = MULTI_CUTTING.get();
			notifyMissingDeps = NOTIFY_MISSING_DEPS.get();
		}
	}

	public static final class Client {
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

		private Client() {
		}

		static void load() {
			bakeDingSound = BAKE_DING_SOUND.get();
			dingVolume = DING_VOLUME.get().floatValue();
		}
	}
}
