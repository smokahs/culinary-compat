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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

public final class PamPotBridge {
	public static final ResourceLocation POT_ITEM_ID = new ResourceLocation(PamCuttingBridge.PAM_MODID, "potitem");
	private static final ResourceLocation FD_COOKING_POT_ID = new ResourceLocation("farmersdelight", "cooking_pot");
	private static final String BRIDGE_NAMESPACE = "culinarycompat";
	private static final String BRIDGE_PATH_PREFIX = "pam_pot_bridge/";
	private static final String BRIDGE_SOURCE = "pam_pot";

	private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
	private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

	private PamPotBridge() {
	}

	public static void setTemplates(List<Recipe<?>> templates) {
		synchronized (TEMPLATES) {
			TEMPLATES.clear();
			TEMPLATES.addAll(templates);
		}
	}

	public static Set<ResourceLocation> getBridgedOutputs() {
		return Collections.unmodifiableSet(BRIDGED_OUTPUTS);
	}

	public static Set<ResourceLocation> getExclusiveBridgeKeys() {
		return Collections.unmodifiableSet(EXCLUSIVE_KEYS);
	}

	public static Set<ResourceLocation> getStrippedRecipeIds() {
		return Collections.unmodifiableSet(STRIPPED_RECIPE_IDS);
	}

	public static void recordStrippedRecipe(ResourceLocation id) {
		STRIPPED_RECIPE_IDS.add(id);
	}

	public static void clearStrippedRecipes() {
		STRIPPED_RECIPE_IDS.clear();
	}

	public static ResourceLocation potItemId() {
		return POT_ITEM_ID;
	}

	public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
		BRIDGED_OUTPUTS.clear();
		EXCLUSIVE_KEYS.clear();
		BridgeRegistry.clearBySource(BRIDGE_SOURCE);

		Item fdPot = ForgeRegistries.ITEMS.getValue(FD_COOKING_POT_ID);
		if (fdPot == null)
			return;
		ItemStack workstationStack = new ItemStack(fdPot);

		Item pamPot = ForgeRegistries.ITEMS.getValue(POT_ITEM_ID);
		if (pamPot == null)
			return;
		ItemStack pamPotProbe = new ItemStack(pamPot);

		List<Recipe<?>> templatesSnapshot;
		synchronized (TEMPLATES) {
			templatesSnapshot = new ArrayList<>(TEMPLATES);
		}
		for (Recipe<?> recipe : templatesSnapshot) {
			if (recipe.getType() != RecipeType.CRAFTING)
				continue;
			if (!(recipe instanceof ShapelessRecipe))
				continue;

			NonNullList<Ingredient> original = recipe.getIngredients();
			if (original.isEmpty())
				continue;

			boolean hasPot = false;
			for (Ingredient ing : original) {
				if (ing != null && ing != Ingredient.EMPTY && ing.test(pamPotProbe)) {
					hasPot = true;
					break;
				}
			}
			if (!hasPot)
				continue;

			ItemStack result = recipe.getResultItem(registries);
			if (result.isEmpty())
				continue;
			if (!result.getItem().isEdible())
				continue;

			NonNullList<Ingredient> stripped = NonNullList.create();
			for (Ingredient ing : original) {
				if (ing == null || ing == Ingredient.EMPTY)
					continue;
				if (ing.test(pamPotProbe))
					continue;
				stripped.add(ing);
			}
			if (stripped.isEmpty())
				continue;

			ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(result.getItem());
			if (outputId == null)
				continue;

			Collection<?> existing = CookingRegistry.getFoodRecipes(outputId);
			boolean outputHadPriorRecipe = !existing.isEmpty();

			ResourceLocation bridgeId = new ResourceLocation(BRIDGE_NAMESPACE,
					BRIDGE_PATH_PREFIX + recipe.getId().getNamespace() + "_" + recipe.getId().getPath());
			int width = 3;
			int height = Math.max(1, (stripped.size() + 2) / 3);
			NonNullList<Ingredient> shaped = NonNullList.withSize(width * height, Ingredient.EMPTY);
			for (int i = 0; i < stripped.size(); i++) {
				shaped.set(i, stripped.get(i));
			}
			ShapedRecipe bridge = new ShapedRecipe(bridgeId, "", CraftingBookCategory.MISC, width, height, shaped,
					result.copy());

			CookingRegistry.addFoodRecipe(bridge, registries);
			BridgeRegistry.register(new BridgeRegistry.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(stripped),
					result.copy(), workstationStack.copy()));
			BRIDGED_OUTPUTS.add(outputId);
			if (!outputHadPriorRecipe) {
				EXCLUSIVE_KEYS.add(outputId);
			}
		}
	}
}
