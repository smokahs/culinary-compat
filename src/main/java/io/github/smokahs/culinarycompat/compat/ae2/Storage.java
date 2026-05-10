package io.github.smokahs.culinarycompat.compat.ae2;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import io.github.smokahs.culinarycompat.CulinaryCompat;

// ae2 side stuff that lets a linked ME network see/extract items from cabinets, fridges, etc in the kitchen
public class Storage implements MEStorage {

	private static Field PROVIDERS_FIELD;
	private static Field CONTAINER_FIELD;

	private final StationBlockEntity station;

	public Storage(StationBlockEntity station) {
		this.station = station;
	}

	@Override
	public Component getDescription() {
		return Component.translatable("block.culinarycompat.ae2_kitchen_station");
	}

	@SuppressWarnings("unchecked")
	private List<IKitchenItemProvider> resolveProviders() {
		Level level = station.getLevel();
		if (!(level instanceof ServerLevel)) {
			return Collections.emptyList();
		}
		BlockPos pos = station.getBlockPos();
		KitchenMultiBlock multi = KitchenMultiBlock.buildFromLocation(level, pos);
		try {
			if (PROVIDERS_FIELD == null) {
				PROVIDERS_FIELD = KitchenMultiBlock.class.getDeclaredField("itemProviderList");
				PROVIDERS_FIELD.setAccessible(true);
			}
			return (List<IKitchenItemProvider>) PROVIDERS_FIELD.get(multi);
		} catch (ReflectiveOperationException e) {
			CulinaryCompat.LOGGER.error("Failed to read CFB KitchenMultiBlock providers", e);
			return Collections.emptyList();
		}
	}

	@Override
	public void getAvailableStacks(KeyCounter out) {
		if (!station.getMode().allowsCFBToAE()) {
			return;
		}
		for (IKitchenItemProvider p : resolveProviders()) {
			if (p instanceof ItemProvider) {
				continue;
			}
			int slots = p.getSlots();
			for (int i = 0; i < slots; i++) {
				ItemStack stack = p.getStackInSlot(i);
				if (!stack.isEmpty()) {
					out.add(AEItemKey.of(stack), stack.getCount());
				}
			}
		}
	}

	@Override
	public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
		if (!station.getMode().allowsCFBToAE()) {
			return 0;
		}
		if (!(what instanceof AEItemKey itemKey)) {
			return 0;
		}
		ItemStack template = itemKey.toStack();
		long extracted = 0;
		boolean simulate = mode == Actionable.SIMULATE;
		for (IKitchenItemProvider p : resolveProviders()) {
			if (p instanceof ItemProvider) {
				continue;
			}
			int slots = p.getSlots();
			for (int i = 0; i < slots && extracted < amount; i++) {
				ItemStack inSlot = p.getStackInSlot(i);
				if (inSlot.isEmpty() || !ItemStack.isSameItemSameTags(inSlot, template)) {
					continue;
				}
				int needed = (int) Math.min(amount - extracted, inSlot.getCount());
				ItemStack got = p.useItemStack(i, needed, simulate, Collections.emptyList(), false);
				if (!got.isEmpty()) {
					extracted += got.getCount();
				}
			}
			if (extracted >= amount) {
				break;
			}
		}
		return extracted;
	}

	@Override
	public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
		if (!station.getMode().allowsCFBToAE()) {
			return 0;
		}
		if (!(what instanceof AEItemKey itemKey)) {
			return 0;
		}
		if (mode == Actionable.SIMULATE) {
			return amount;
		}
		long inserted = 0;
		long remaining = amount;
		int maxStack = itemKey.toStack().getMaxStackSize();
		for (IKitchenItemProvider p : resolveProviders()) {
			if (p instanceof ItemProvider) {
				continue;
			}
			Container container = resolveContainer(p);
			if (container == null) {
				continue;
			}
			while (remaining > 0) {
				int chunk = (int) Math.min(remaining, maxStack);
				ItemStack toInsert = itemKey.toStack(chunk);
				int accepted = insertIntoContainer(container, toInsert);
				if (accepted <= 0) {
					break;
				}
				inserted += accepted;
				remaining -= accepted;
			}
			if (remaining <= 0) {
				break;
			}
		}
		return inserted;
	}

	// allows writing to kitchen mutliblock-blocks directly
	private static Container resolveContainer(IKitchenItemProvider provider) {
		if (!(provider instanceof DefaultKitchenItemProvider)) {
			return null;
		}
		try {
			if (CONTAINER_FIELD == null) {
				CONTAINER_FIELD = DefaultKitchenItemProvider.class.getDeclaredField("container");
				CONTAINER_FIELD.setAccessible(true);
			}
			return (Container) CONTAINER_FIELD.get(provider);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	private static int insertIntoContainer(Container c, ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		int initialCount = stack.getCount();
		int maxStack = Math.min(stack.getMaxStackSize(), c.getMaxStackSize());
		// merge into existing matching stacks.
		for (int i = 0; i < c.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack inSlot = c.getItem(i);
			if (inSlot.isEmpty() || !ItemStack.isSameItemSameTags(inSlot, stack) || !c.canPlaceItem(i, stack)) {
				continue;
			}
			int space = maxStack - inSlot.getCount();
			if (space <= 0) {
				continue;
			}
			int put = Math.min(space, stack.getCount());
			inSlot.grow(put);
			stack.shrink(put);
		}
		// empty slots
		for (int i = 0; i < c.getContainerSize() && !stack.isEmpty(); i++) {
			if (!c.getItem(i).isEmpty() || !c.canPlaceItem(i, stack)) {
				continue;
			}
			int put = Math.min(maxStack, stack.getCount());
			ItemStack newSlot = stack.copy();
			newSlot.setCount(put);
			c.setItem(i, newSlot);
			stack.shrink(put);
		}
		int accepted = initialCount - stack.getCount();
		if (accepted > 0) {
			c.setChanged();
		}
		return accepted;
	}
}
