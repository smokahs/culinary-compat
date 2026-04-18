package com.hoshihoku.culinarycompat.compat.cfb.bake;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.config.ClientConfig;
import com.hoshihoku.culinarycompat.network.BakePhase;
import com.hoshihoku.culinarycompat.registry.ModSounds;

@OnlyIn(Dist.CLIENT)
public final class ClientBakeState {
	private static final ResourceLocation CFB_OVEN_OPEN = new ResourceLocation("cookingforblockheads", "oven_open");
	private static BakePhase phase = null;
	private static ResourceLocation outputId = null;

	private ClientBakeState() {
	}

	public static BakePhase getPhase() {
		return phase;
	}

	public static ResourceLocation getOutputId() {
		return outputId;
	}

	public static void onStatus(BakePhase newPhase, ResourceLocation newOutputId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		switch (newPhase) {
			case CONFIRM -> {
				phase = BakePhase.CONFIRM;
				outputId = newOutputId;
				playSound(ForgeRegistries.SOUND_EVENTS.getValue(CFB_OVEN_OPEN), 1.0f);
			}
			case BAKING -> {
				phase = BakePhase.BAKING;
				outputId = newOutputId;
				playSound(SoundEvents.CAMPFIRE_CRACKLE, 1.0f);
			}
			case FINISHED -> {
				phase = null;
				outputId = null;
				if (ClientConfig.bakeDingSound && ClientConfig.dingVolume > 0.0f) {
					playSound(ModSounds.DING.get(), ClientConfig.dingVolume);
				}
			}
			case CANCELLED -> {
				phase = null;
				outputId = null;
				mc.player.displayClientMessage(Component.literal("Recipe cancelled!").withStyle(ChatFormatting.RED),
						true);
			}
		}
	}

	private static void playSound(SoundEvent sound, float volume) {
		if (sound == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		mc.player.playSound(sound, volume, 1.0f);
	}
}
