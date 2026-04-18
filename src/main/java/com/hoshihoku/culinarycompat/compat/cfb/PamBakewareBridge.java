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

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;
import com.hoshihoku.culinarycompat.registry.ModItems;

public final class PamBakewareBridge {
	public static final ResourceLocation PAM_BAKEWARE_ITEM_ID = new ResourceLocation(PamCuttingBridge.PAM_MODID,
			"bakewareitem");
	private static final String BRIDGE_NAMESPACE = "culinarycompat";
	private static final String BRIDGE_PATH_PREFIX = "pam_bakeware_bridge/";
	private static final String BRIDGE_SOURCE = "pam_bakeware";

	private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
	private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

	private PamBakewareBridge() {
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

	public static ResourceLocation bakewareItemId() {
		return PAM_BAKEWARE_ITEM_ID;
	}

	public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
		BRIDGED_OUTPUTS.clear();
		EXCLUSIVE_KEYS.clear();
		BridgeRegistry.clearBySource(BRIDGE_SOURCE);

		Item ourBakeware = ModItems.BAKEWARE.get();
		if (ourBakeware == null)
			return;
		ItemStack workstationStack = new ItemStack(ourBakeware);

		Item pamBakeware = ForgeRegistries.ITEMS.getValue(PAM_BAKEWARE_ITEM_ID);
		ItemStack pamBakewareProbe = pamBakeware == null ? ItemStack.EMPTY : new ItemStack(pamBakeware);

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

			boolean hasBakeware = false;
			for (Ingredient ing : original) {
				if (ing == null || ing == Ingredient.EMPTY)
					continue;
				if (ing.test(new ItemStack(ourBakeware))) {
					hasBakeware = true;
					break;
				}
				if (!pamBakewareProbe.isEmpty() && ing.test(pamBakewareProbe)) {
					hasBakeware = true;
					break;
				}
			}
			if (!hasBakeware)
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
				boolean isBakeware = ing.test(new ItemStack(ourBakeware))
						|| (!pamBakewareProbe.isEmpty() && ing.test(pamBakewareProbe));
				if (isBakeware)
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
		CulinaryCompat.LOGGER.info("Pam bakeware bridge: registered {} recipes", BRIDGED_OUTPUTS.size());
	}
}
