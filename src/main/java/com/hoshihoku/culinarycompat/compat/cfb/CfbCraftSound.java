package com.hoshihoku.culinarycompat.compat.cfb;

import net.blay09.mods.cookingforblockheads.menu.inventory.InventoryCraftBook;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

public final class CfbCraftSound {
	private static final ResourceLocation KNIFE_SOUND_ID = new ResourceLocation(CfbIntegration.FD_MODID,
			"block.cutting_board.knife");
	private static final ResourceLocation SKILLET_SOUND_ID = new ResourceLocation(CfbIntegration.FD_MODID,
			"block.skillet.add_food");
	private static final ResourceLocation POT_SOUND_ID = new ResourceLocation(CfbIntegration.FD_MODID,
			"block.cooking_pot.boil_soup");

	private CfbCraftSound() {
	}

	@SubscribeEvent
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		if (!(event.getInventory() instanceof InventoryCraftBook))
			return;

		ItemStack crafted = event.getCrafting();
		if (crafted.isEmpty())
			return;
		ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(crafted.getItem());
		if (outputId == null)
			return;

		ResourceLocation soundId = resolveSound(outputId);

		Player player = event.getEntity();
		Level level = player.level();
		if (level.isClientSide)
			return;

		if (FdPotBridge.getBridgedOutputs().contains(outputId)) {
			float xp = BridgeRegistry.findExperienceFor("fd_cooking", outputId);
			if (xp > 0f) {
				player.giveExperiencePoints(Math.round(xp));
			}
		}

		if (soundId == null)
			return;
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
		if (sound == null)
			return;

		float pitch = 0.8f + level.random.nextFloat() * 0.4f;
		level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.BLOCKS, 0.8f, pitch);
	}

	private static ResourceLocation resolveSound(ResourceLocation outputId) {
		if (PamPotBridge.getBridgedOutputs().contains(outputId) || FdPotBridge.getBridgedOutputs().contains(outputId)) {
			return POT_SOUND_ID;
		}
		if (PamSkilletBridge.getBridgedOutputs().contains(outputId)
				|| CfbCampfireBridge.getBridgedOutputs().contains(outputId)) {
			return SKILLET_SOUND_ID;
		}
		if (CfbCuttingBridge.getBridgedOutputs().contains(outputId)
				|| PamCuttingBridge.getBridgedOutputs().contains(outputId)) {
			return KNIFE_SOUND_ID;
		}
		return null;
	}
}
