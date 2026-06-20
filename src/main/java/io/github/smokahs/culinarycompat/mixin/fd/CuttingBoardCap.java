package io.github.smokahs.culinarycompat.mixin.fd;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

// fixes some issues between 1.2.x and 1.3.x

@Mixin(value = CuttingBoardBlockEntity.class, remap = false)
public abstract class CuttingBoardCap {
	@Shadow
	private ItemStackHandler inventory;

	@Shadow
	private boolean isItemCarvingBoard;

	// cap slot 0 to 1
	@Inject(method = "addItem", at = @At("HEAD"), cancellable = true)
	private void culinarycompat$capAddItem(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
		if (itemStack.isEmpty() || isItemCarvingBoard) {
			return;
		}
		if (inventory.getStackInSlot(0).isEmpty()) {
			inventory.setStackInSlot(0, itemStack.split(1));
			((SyncedBlockEntityAccessor) (Object) this).culinarycompat$invokeInventoryChanged();
			cir.setReturnValue(culinarycompat$leftover(itemStack));
			return;
		}
		// slot 0 already holds the single ingredient; reject like FD native so
		// carveToolOnBoard fails on a full board
		cir.setReturnValue(itemStack);
	}

	// carveToolOnBoard reference-compares the result against ItemStack.EMPTY, so
	// hand back the singleton when consumed
	private static ItemStack culinarycompat$leftover(ItemStack stack) {
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}
}
