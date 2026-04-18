package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.function.Predicate;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

public class ToolBridgeShapelessRecipe extends ShapelessRecipe {
	private final Predicate<ItemStack> toolTest;
	private final ResourceLocation immuneItemId;

	public ToolBridgeShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result,
			NonNullList<Ingredient> ingredients, Predicate<ItemStack> toolTest, ResourceLocation immuneItemId) {
		super(id, group, category, result, ingredients);
		this.toolTest = toolTest;
		this.immuneItemId = immuneItemId;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		for (int i = 0; i < remaining.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty())
				continue;
			if (toolTest != null && toolTest.test(stack)) {
				remaining.set(i, preserveTool(stack));
			} else if (stack.hasCraftingRemainingItem()) {
				remaining.set(i, stack.getCraftingRemainingItem());
			}
		}
		return remaining;
	}

	private ItemStack preserveTool(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.setCount(1);
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(copy.getItem());
		if ((immuneItemId != null && immuneItemId.equals(id)) || !copy.isDamageableItem()) {
			return copy;
		}
		int newDamage = copy.getDamageValue() + 1;
		if (newDamage >= copy.getMaxDamage()) {
			return ItemStack.EMPTY;
		}
		copy.setDamageValue(newDamage);
		return copy;
	}
}
