package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

public final class CfbCampfireBridge {
	private static final ResourceLocation CFB_OVEN_ID = new ResourceLocation("cookingforblockheads", "oven");
	private static final String BRIDGE_SOURCE = "campfire_oven";

	private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> HIDDEN_CAMPFIRE_RECIPE_IDS = ConcurrentHashMap.newKeySet();

	private CfbCampfireBridge() {
	}

	public static Set<ResourceLocation> getBridgedOutputs() {
		return Collections.unmodifiableSet(BRIDGED_OUTPUTS);
	}

	public static Set<ResourceLocation> getHiddenCampfireRecipeIds() {
		return Collections.unmodifiableSet(HIDDEN_CAMPFIRE_RECIPE_IDS);
	}

	public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
		BRIDGED_OUTPUTS.clear();
		HIDDEN_CAMPFIRE_RECIPE_IDS.clear();
		BridgeRegistry.clearBySource(BRIDGE_SOURCE);

		Item ovenItem = ForgeRegistries.ITEMS.getValue(CFB_OVEN_ID);
		if (ovenItem == null)
			return;
		ItemStack workstationStack = new ItemStack(ovenItem);

		Set<ResourceLocation> smeltingOutputs = new HashSet<>();
		for (SmeltingRecipe sr : recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
			ItemStack out = sr.getResultItem(registries);
			if (out.isEmpty())
				continue;
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(out.getItem());
			if (id != null)
				smeltingOutputs.add(id);
		}

		Set<ResourceLocation> craftingOutputs = CraftingOutputIndex.collect(recipeManager, registries);

		List<CampfireCookingRecipe> campfireRecipes = recipeManager.getAllRecipesFor(RecipeType.CAMPFIRE_COOKING);
		for (CampfireCookingRecipe recipe : campfireRecipes) {
			NonNullList<Ingredient> ings = recipe.getIngredients();
			if (ings.isEmpty())
				continue;
			Ingredient input = ings.get(0);
			if (input == null || input == Ingredient.EMPTY)
				continue;

			ItemStack result = recipe.getResultItem(registries);
			if (result.isEmpty())
				continue;

			ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(result.getItem());
			if (outputId == null)
				continue;

			if (smeltingOutputs.contains(outputId)) {
				HIDDEN_CAMPFIRE_RECIPE_IDS.add(recipe.getId());
				continue;
			}

			if (craftingOutputs.contains(outputId)) {
				HIDDEN_CAMPFIRE_RECIPE_IDS.add(recipe.getId());
				continue;
			}

			CookingRegistry.addFoodRecipe(recipe, registries);
			BridgeRegistry.register(new BridgeRegistry.Entry(BRIDGE_SOURCE, recipe.getId(), new ArrayList<>(ings),
					result.copy(), workstationStack.copy()));
			BRIDGED_OUTPUTS.add(outputId);
			HIDDEN_CAMPFIRE_RECIPE_IDS.add(recipe.getId());
		}
	}
}
