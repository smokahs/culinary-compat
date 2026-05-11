package io.github.smokahs.culinarycompat.compat.cfb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

// stop eating bro!!!!
public final class CuttingBoardInteract {
	private static final TagKey<Item> KNIVES = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tools/knives"));

	private CuttingBoardInteract() {
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (!player.isCrouching()) {
			return;
		}
		ItemStack held = event.getItemStack();
		if (held.isEmpty() || held.is(KNIVES)) {
			return;
		}
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof CuttingBoardBlockEntity board)) {
			return;
		}
		if (!board.isEmpty()) {
			return;
		}

		IItemHandler inv = board.getInventory();
		ItemStack toInsert = held.copy();
		int before = toInsert.getCount();
		ItemStack remainder = inv.insertItem(0, toInsert, false);
		int placed = before - remainder.getCount();
		if (placed <= 0) {
			return;
		}
		if (!player.getAbilities().instabuild) {
			held.shrink(placed);
		}
		level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.WOOD_PLACE,
				SoundSource.BLOCKS, 1.0f, 0.8f);
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
	}
}
