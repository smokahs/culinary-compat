package io.github.smokahs.culinarycompat.mixin.cfb;

import java.util.List;

import net.blay09.mods.cookingforblockheads.api.SourceItem;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// bottle-sized water items drain a quarter bucket from the sink tank
@Mixin(targets = "net.blay09.mods.cookingforblockheads.tile.SinkBlockEntity$SinkItemProvider", remap = false)
public abstract class SinkProvider {
	@Shadow(remap = false)
	@Final
	private NonNullList<ItemStack> itemStacks;

	@Unique
	private int culinarycompat$cost(int slot) {
		return culinarycompat$costOf(itemStacks.get(slot));
	}

	@Unique
	private int culinarycompat$costOf(ItemStack stack) {
		return stack.getItem() == Items.WATER_BUCKET || stack.getItem() == Items.MILK_BUCKET ? 1000 : 250;
	}

	@ModifyConstant(method = "useItemStack", constant = @Constant(intValue = 1000), remap = false, require = 0)
	private int culinarycompat$useCost(int constant, int slot, int amount, boolean simulate,
			List<IKitchenItemProvider> inventories, boolean requireBucket) {
		return culinarycompat$cost(slot);
	}

	@ModifyConstant(method = "getSimulatedUseCount", constant = @Constant(intValue = 1000), remap = false, require = 0)
	private int culinarycompat$simulatedCost(int constant, int slot) {
		return culinarycompat$cost(slot);
	}

	@ModifyConstant(method = "getStackInSlot", constant = @Constant(intValue = 1000), remap = false, require = 0)
	private int culinarycompat$slotCost(int constant, int slot) {
		return culinarycompat$cost(slot);
	}

	@ModifyConstant(method = "returnItemStack", constant = @Constant(intValue = 1000), remap = false, require = 0)
	private int culinarycompat$returnCost(int constant, ItemStack itemStack, SourceItem sourceItem) {
		return culinarycompat$costOf(itemStack);
	}
}
