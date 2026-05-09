package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.blay09.mods.cookingforblockheads.menu.inventory.InventoryCraftBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.Bridges;

public final class UI {
	private UI() {
	}

	public static final class CraftSound {
		private static final ResourceLocation KNIFE_SOUND_ID = new ResourceLocation(CFB.FD_MODID,
				"block.cutting_board.knife");
		private static final ResourceLocation SKILLET_SOUND_ID = new ResourceLocation(CFB.FD_MODID,
				"block.skillet.add_food");
		private static final ResourceLocation POT_SOUND_ID = new ResourceLocation(CFB.FD_MODID,
				"block.cooking_pot.boil_soup");

		private CraftSound() {
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

			if (FarmersDelight.Pot.getBridgedOutputs().contains(outputId)) {
				float xp = Bridges.findExperienceFor("fd_cooking", outputId);
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
			if (Pam.Pot.getBridgedOutputs().contains(outputId)
					|| FarmersDelight.Pot.getBridgedOutputs().contains(outputId)) {
				return POT_SOUND_ID;
			}
			if (Pam.Skillet.getBridgedOutputs().contains(outputId)
					|| FarmersDelight.Campfire.getBridgedOutputs().contains(outputId)) {
				return SKILLET_SOUND_ID;
			}
			if (FarmersDelight.Cutting.getBridgedOutputs().contains(outputId)
					|| Pam.Cutting.getBridgedOutputs().contains(outputId)) {
				return KNIFE_SOUND_ID;
			}
			return null;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static final class Tooltip {
		private static final Set<ResourceLocation> KITCHEN_MEMBER_ITEMS = Set.of(
				new ResourceLocation(CFB.FD_MODID, "cutting_board"), new ResourceLocation(CFB.FD_MODID, "stove"),
				new ResourceLocation(CFB.FD_MODID, "skillet"), new ResourceLocation(CFB.FD_MODID, "cooking_pot"),
				new ResourceLocation("culinarycompat", "bakeware"));
		private static final ResourceLocation FD_NETHERITE_KNIFE = new ResourceLocation(CFB.FD_MODID,
				"netherite_knife");
		private static final String TOOLTIP_KEY = "tooltip.cookingforblockheads:multiblock_kitchen";

		private Tooltip() {
		}

		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event) {
			ItemStack stack = event.getItemStack();
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (id == null)
				return;
			if (KITCHEN_MEMBER_ITEMS.contains(id)) {
				List<Component> tip = event.getToolTip();
				// skip if cfb already added the line natively
				boolean already = false;
				for (Component line : tip) {
					if (line.getContents() instanceof TranslatableContents tc && TOOLTIP_KEY.equals(tc.getKey())) {
						already = true;
						break;
					}
				}
				if (!already) {
					tip.add(Component.translatable(TOOLTIP_KEY).withStyle(ChatFormatting.YELLOW));
				}
			} else if (FD_NETHERITE_KNIFE.equals(id)) {
				event.getToolTip().add(
						Component.literal("Never dulls when used in a kitchen.").withStyle(ChatFormatting.DARK_PURPLE));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static final class MissingToolsTooltip {
		private static final String CFB_MISSING_TOOLS_KEY = "tooltip.cookingforblockheads:missing_tools";

		private MissingToolsTooltip() {
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onItemTooltip(ItemTooltipEvent event) {
			List<Component> tooltip = event.getToolTip();
			int idx = -1;
			for (int i = 0; i < tooltip.size(); i++) {
				Component line = tooltip.get(i);
				if (line.getContents() instanceof TranslatableContents tc
						&& CFB_MISSING_TOOLS_KEY.equals(tc.getKey())) {
					idx = i;
					break;
				}
			}
			if (idx < 0)
				return;

			ItemStack stack = event.getItemStack();
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (id == null)
				return;

			List<String> tools = new ArrayList<>();
			Set<ResourceLocation> fdCutting = FarmersDelight.Cutting.getBridgedOutputs();
			Set<ResourceLocation> pamCutting = Pam.Cutting.getBridgedOutputs();
			Set<ResourceLocation> skillet = Pam.Skillet.getBridgedOutputs();
			Set<ResourceLocation> pamPot = Pam.Pot.getBridgedOutputs();
			Set<ResourceLocation> fdPot = FarmersDelight.Pot.getBridgedOutputs();
			Set<ResourceLocation> bakeware = Pam.Bakeware.getBridgedOutputs();
			if (fdCutting.contains(id) || pamCutting.contains(id))
				tools.add("Cutting Board");
			if (skillet.contains(id))
				tools.add("Skillet");
			if (pamPot.contains(id) || fdPot.contains(id))
				tools.add("Cooking Pot");
			if (bakeware.contains(id))
				tools.add("Bakeware");

			if (tools.isEmpty())
				return;

			MutableComponent replacement = Component.translatable(CFB_MISSING_TOOLS_KEY)
					.append(Component.literal(": " + String.join(", ", tools))).withStyle(ChatFormatting.RED);
			tooltip.set(idx, replacement);
		}
	}
}
