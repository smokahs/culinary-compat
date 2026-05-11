package io.github.smokahs.culinarycompat.mixin.compat;

import net.minecraftforge.items.IItemHandler;

import com.axedgaming.endersdelight.block.entity.EndstoneStoveBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// fixes for endersdelight (Ax3dGaming/EndersDelight)
@Mixin(value = EndstoneStoveBlockEntity.class, remap = false)
public abstract class EndersDelight {
	// adds back removed FD ItemUtils.isInventoryEmpty to prevent ticking crash on FD 1.3.1+
	@Redirect(method = "cookingTick", at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;isInventoryEmpty(Lnet/minecraftforge/items/IItemHandler;)Z"), require = 0)
	private static boolean culinarycompat$redirectIsInventoryEmpty(IItemHandler handler) {
		for (int i = 0; i < handler.getSlots(); i++) {
			if (!handler.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
