package io.github.smokahs.culinarycompat.mixin.fd;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.smokahs.culinarycompat.recipe.MultiCuttingExtras;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

@Mixin(value = CuttingBoardBlock.class, remap = false)
public abstract class CuttingBoardUse {
	private static final TagKey<Item> CULINARYCOMPAT$KNIVES_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tools/knives"));

	@Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
	private void culinarycompat$multiUse(BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof CuttingBoardBlockEntity board)) {
			return;
		}
		ItemStack held = player.getItemInHand(hand);
		boolean isMain = hand == InteractionHand.MAIN_HAND;

		if (held.isEmpty() && isMain && player.isCrouching() && !board.isEmpty()) {
			while (!board.isEmpty()) {
				ItemStack item = board.removeItem();
				if (item.isEmpty()) {
					break;
				}
				if (!player.isCreative() && !player.getInventory().add(item)) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), item);
				}
			}
			level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ITEM_PICKUP,
					SoundSource.BLOCKS, 0.5f, 0.8f);
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		if (!held.isEmpty() && !held.is(CULINARYCOMPAT$KNIVES_TAG) && !board.isEmpty()
				&& board.getInventory().getStackInSlot(0).getCount() <= 1) {
			IItemHandler extras = ((MultiCuttingExtras) board).culinarycompat$getExtras();
			for (int i = 0; i < extras.getSlots(); i++) {
				if (!extras.getStackInSlot(i).isEmpty()) {
					continue;
				}
				ItemStack one = player.getAbilities().instabuild ? held.copy().split(1) : held.split(1);
				extras.insertItem(i, one, false);
				level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.WOOD_PLACE,
						SoundSource.BLOCKS, 1.0f, 0.8f);
				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
		}
	}

	@Inject(method = "onRemove", at = @At("HEAD"), remap = true)
	private void culinarycompat$dropAllSlots(BlockState state, Level level, BlockPos pos, BlockState newState,
			boolean isMoving, CallbackInfo ci) {
		if (state.is(newState.getBlock())) {
			return;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof CuttingBoardBlockEntity board)) {
			return;
		}
		if (board instanceof MultiCuttingExtras provider) {
			IItemHandler extras = provider.culinarycompat$getExtras();
			for (int i = 0; i < extras.getSlots(); i++) {
				ItemStack s = extras.getStackInSlot(i);
				if (!s.isEmpty()) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s);
				}
			}
		}
	}
}
