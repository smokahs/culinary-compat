package io.github.smokahs.culinarycompat.mixin.fd;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

@Mixin(value = SyncedBlockEntity.class, remap = false)
public interface SyncedBlockEntityAccessor {
	@Invoker("inventoryChanged")
	void culinarycompat$invokeInventoryChanged();
}
