package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

public final class FdPotBridge {
	private static final ResourceLocation FD_COOKING_POT_ID = new ResourceLocation("farmersdelight", "cooking_pot");
	private static final ResourceLocation FD_COOKING_TYPE_ID = new ResourceLocation("farmersdelight", "cooking");
	private static final String BRIDGE_NAMESPACE = "culinarycompat";
	private static final String BRIDGE_PATH_PREFIX = "fd_cooking_bridge/";
	private static final String BRIDGE_SOURCE = "fd_cooking";

	private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();

	private FdPotBridge() {
	}

	public static Set<ResourceLocation> getBridgedOutputs() {
		return Collections.unmodifiableSet(BRIDGED_OUTPUTS);
	}

	public static Set<ResourceLocation> getExclusiveBridgeKeys() {
		return Collections.unmodifiableSet(EXCLUSIVE_KEYS);
	}

	@SuppressWarnings("unchecked")
	public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
		BRIDGED_OUTPUTS.clear();
		EXCLUSIVE_KEYS.clear();
		BridgeRegistry.clearBySource(BRIDGE_SOURCE);

		Item fdPot = ForgeRegistries.ITEMS.getValue(FD_COOKING_POT_ID);
		if (fdPot == null)
			return;
		ItemStack workstationStack = new ItemStack(fdPot);

		RecipeType<CookingPotRecipe> cookingType = (RecipeType<CookingPotRecipe>) BuiltInRegistries.RECIPE_TYPE
				.get(FD_COOKING_TYPE_ID);
		if (cookingType == null)
			return;

		Set<ResourceLocation> craftingOutputs = CraftingOutputIndex.collect(recipeManager, registries);

		List<CookingPotRecipe> recipes = recipeManager.getAllRecipesFor(cookingType);
		for (CookingPotRecipe recipe : recipes) {
			ItemStack result = recipe.getResultItem(registries);
			if (result.isEmpty())
				continue;

			NonNullList<Ingredient> original = recipe.getIngredients();
			if (original.isEmpty())
				continue;

			ItemStack container = recipe.getOutputContainer();
			int size = original.size() + (container.isEmpty() ? 0 : 1);
			NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(size);
			for (Ingredient ing : original) {
				ingredients.add(ing == null ? Ingredient.EMPTY : ing);
			}
			if (!container.isEmpty()) {
				ingredients.add(Ingredient.of(container));
			}

			ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(result.getItem());
			if (outputId == null)
				continue;

			if (craftingOutputs.contains(outputId))
				continue;

			Collection<?> existing = CookingRegistry.getFoodRecipes(outputId);
			boolean outputHadPriorRecipe = !existing.isEmpty();

			ResourceLocation bridgeId = new ResourceLocation(BRIDGE_NAMESPACE,
					BRIDGE_PATH_PREFIX + recipe.getId().getNamespace() + "_" + recipe.getId().getPath());
			ShapelessRecipe bridge = new ShapelessRecipe(bridgeId, "", CraftingBookCategory.MISC, result.copy(),
					ingredients);

			CookingRegistry.addFoodRecipe(bridge, registries);
			BridgeRegistry.register(new BridgeRegistry.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(ingredients),
					result.copy(), workstationStack.copy(), recipe.getExperience()));
			BRIDGED_OUTPUTS.add(outputId);
			if (!outputHadPriorRecipe) {
				EXCLUSIVE_KEYS.add(outputId);
			}
		}
	}
}
