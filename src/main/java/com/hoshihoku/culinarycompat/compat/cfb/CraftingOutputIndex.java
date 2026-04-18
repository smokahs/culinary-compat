package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

public final class CraftingOutputIndex {
	private CraftingOutputIndex() {
	}

	public static Set<ResourceLocation> collect(RecipeManager recipeManager, RegistryAccess registries) {
		Set<ResourceLocation> outputs = new HashSet<>();
		for (Recipe<?> recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
			ItemStack result = recipe.getResultItem(registries);
			if (result.isEmpty())
				continue;
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(result.getItem());
			if (id != null)
				outputs.add(id);
		}
		return outputs;
	}
}
