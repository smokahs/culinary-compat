package com.hoshihoku.culinarycompat.compat.cfb.bake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.blay09.mods.cookingforblockheads.client.gui.screen.RecipeBookScreen;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.compat.cfb.PamBakewareBridge;
import com.hoshihoku.culinarycompat.config.ModConfigs;
import com.hoshihoku.culinarycompat.network.NetworkHandler;
import com.hoshihoku.culinarycompat.network.NetworkHandler.BakePhase;
import com.hoshihoku.culinarycompat.network.NetworkHandler.BakeStatusPacket;
import com.hoshihoku.culinarycompat.registry.ModRegistry;

public final class BakeState {
	private BakeState() {
	}

	public static final class Pending {
		public BakePhase phase;
		public ResourceLocation outputId;
		public ItemStack output;
		public List<ItemStack> ingredients;
		public long startTick;

		public Pending(BakePhase phase, ResourceLocation outputId) {
			this.phase = phase;
			this.outputId = outputId;
			this.output = ItemStack.EMPTY;
			this.ingredients = List.of();
			this.startTick = 0L;
		}
	}

	public static final class Manager {
		private static final Map<UUID, Pending> PENDING = new HashMap<>();

		private Manager() {
		}

		public static int getBakeDurationTicks() {
			return Math.max(0, ModConfigs.Common.bakewareDurationTicks);
		}

		public static Pending get(UUID playerId) {
			return PENDING.get(playerId);
		}

		public static void setConfirm(ServerPlayer player, ResourceLocation outputId) {
			Pending p = new Pending(BakePhase.CONFIRM, outputId);
			PENDING.put(player.getUUID(), p);
			NetworkHandler.sendToClient(player, new BakeStatusPacket(BakePhase.CONFIRM, outputId));
		}

		public static void setBaking(ServerPlayer player, ResourceLocation outputId, ItemStack output,
				List<ItemStack> ingredients) {
			Pending p = new Pending(BakePhase.BAKING, outputId);
			p.output = output.copy();
			p.ingredients = ingredients;
			p.startTick = player.serverLevel().getGameTime();
			PENDING.put(player.getUUID(), p);
			if (!output.isEmpty()) {
				player.getCooldowns().addCooldown(output.getItem(), getBakeDurationTicks());
			}
			NetworkHandler.sendToClient(player, new BakeStatusPacket(BakePhase.BAKING, outputId));
		}

		public static void clear(UUID playerId) {
			PENDING.remove(playerId);
		}

		@SubscribeEvent
		public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
			if (event.phase != TickEvent.Phase.END)
				return;
			if (!(event.player instanceof ServerPlayer sp))
				return;
			Pending p = PENDING.get(sp.getUUID());
			if (p == null || p.phase != BakePhase.BAKING)
				return;
			long elapsed = sp.serverLevel().getGameTime() - p.startTick;
			if (elapsed < getBakeDurationTicks())
				return;

			ItemStack out = p.output;
			if (!out.isEmpty()) {
				if (!sp.getInventory().add(out)) {
					sp.drop(out, false);
				}
			}
			NetworkHandler.sendToClient(sp, new BakeStatusPacket(BakePhase.FINISHED, p.outputId));
			PENDING.remove(sp.getUUID());
		}

		@SubscribeEvent
		public static void onContainerClose(PlayerContainerEvent.Close event) {
			if (!(event.getEntity() instanceof ServerPlayer sp))
				return;
			Pending p = PENDING.get(sp.getUUID());
			if (p == null)
				return;
			if (!(event.getContainer() instanceof RecipeBookMenu))
				return;

			if (p.phase == BakePhase.BAKING) {
				if (ModConfigs.Common.bakewareRefundOnCancel) {
					for (ItemStack ing : p.ingredients) {
						if (ing == null || ing.isEmpty())
							continue;
						ItemStack copy = ing.copy();
						if (!sp.getInventory().add(copy)) {
							sp.drop(copy, false);
						}
					}
				}
				if (p.output != null && !p.output.isEmpty()) {
					sp.getCooldowns().removeCooldown(p.output.getItem());
				}
			}
			NetworkHandler.sendToClient(sp, new BakeStatusPacket(BakePhase.CANCELLED, p.outputId));
			PENDING.remove(sp.getUUID());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static final class Client {
		private static final ResourceLocation CFB_OVEN_OPEN = new ResourceLocation("cookingforblockheads", "oven_open");
		private static BakePhase phase = null;
		private static ResourceLocation outputId = null;

		private Client() {
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
					if (ModConfigs.Client.bakeDingSound && ModConfigs.Client.dingVolume > 0.0f) {
						playSound(ModRegistry.Sounds.DING.get(), ModConfigs.Client.dingVolume);
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

	@OnlyIn(Dist.CLIENT)
	public static final class Tooltip {
		private static final String CFB_CLICK_CRAFT_ONE = "tooltip.cookingforblockheads:click_to_craft_one";
		private static final String CFB_CLICK_CRAFT_STACK = "tooltip.cookingforblockheads:click_to_craft_stack";

		private Tooltip() {
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onItemTooltip(ItemTooltipEvent event) {
			if (!(Minecraft.getInstance().screen instanceof RecipeBookScreen))
				return;

			ItemStack stack = event.getItemStack();
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (id == null)
				return;

			boolean isBakeware = PamBakewareBridge.getExclusiveBridgeKeys().contains(id);
			if (isBakeware && ModConfigs.Common.bakewareEnabled) {
				List<Component> tip = event.getToolTip();
				tip.removeIf(line -> line.getContents() instanceof TranslatableContents tc
						&& (CFB_CLICK_CRAFT_ONE.equals(tc.getKey()) || CFB_CLICK_CRAFT_STACK.equals(tc.getKey())));
			}

			BakePhase phase = Client.getPhase();
			if (phase == null)
				return;
			ResourceLocation pendingId = Client.getOutputId();
			if (pendingId == null || !pendingId.equals(id))
				return;

			if (phase == BakePhase.CONFIRM) {
				event.getToolTip().add(Component.literal("Click again to bake!").withStyle(ChatFormatting.GOLD));
			} else if (phase == BakePhase.BAKING) {
				event.getToolTip().add(Component.literal("Baking\u2026").withStyle(ChatFormatting.GOLD));
			}
		}
	}
}
