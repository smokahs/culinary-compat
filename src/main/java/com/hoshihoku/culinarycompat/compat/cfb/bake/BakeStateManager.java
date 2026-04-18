package com.hoshihoku.culinarycompat.compat.cfb.bake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.hoshihoku.culinarycompat.config.CommonConfig;
import com.hoshihoku.culinarycompat.network.BakePhase;
import com.hoshihoku.culinarycompat.network.BakeStatusPacket;
import com.hoshihoku.culinarycompat.network.NetworkHandler;

public final class BakeStateManager {
	private static final Map<UUID, PendingBake> PENDING = new HashMap<>();

	public static int getBakeDurationTicks() {
		return Math.max(0, CommonConfig.bakewareDurationTicks);
	}

	private BakeStateManager() {
	}

	public static PendingBake get(UUID playerId) {
		return PENDING.get(playerId);
	}

	public static void setConfirm(ServerPlayer player, ResourceLocation outputId) {
		PendingBake p = new PendingBake(BakePhase.CONFIRM, outputId);
		PENDING.put(player.getUUID(), p);
		NetworkHandler.sendToClient(player, new BakeStatusPacket(BakePhase.CONFIRM, outputId));
	}

	public static void setBaking(ServerPlayer player, ResourceLocation outputId, ItemStack output,
			List<ItemStack> ingredients) {
		PendingBake p = new PendingBake(BakePhase.BAKING, outputId);
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
		PendingBake p = PENDING.get(sp.getUUID());
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
		PendingBake p = PENDING.get(sp.getUUID());
		if (p == null)
			return;
		if (!(event.getContainer() instanceof RecipeBookMenu))
			return;

		if (p.phase == BakePhase.BAKING) {
			if (CommonConfig.bakewareRefundOnCancel) {
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
