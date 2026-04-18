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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

public final class PamCuttingBridge {
	public static final String PAM_MODID = "pamhc2foodcore";
	private static final ResourceLocation CUTTINGBOARD_TAG_ID = new ResourceLocation("forge", "tool_cuttingboard");
	private static final ResourceLocation KNIVES_TAG_ID = new ResourceLocation("forge", "tools/knives");
	private static final ResourceLocation CUTTINGBOARD_ITEM_ID = new ResourceLocation(PAM_MODID, "cuttingboarditem");
	private static final String BRIDGE_NAMESPACE = "culinarycompat";
	private static final String BRIDGE_PATH_PREFIX = "pam_cutting_bridge/";

	private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
	private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

	public static void setTemplates(List<Recipe<?>> templates) {
		synchronized (TEMPLATES) {
			TEMPLATES.clear();
			TEMPLATES.addAll(templates);
		}
	}

	private PamCuttingBridge() {
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

	private static final String BRIDGE_SOURCE = "pam_cuttingboard";
	private static final ResourceLocation FD_CUTTING_BOARD_ID = new ResourceLocation("farmersdelight", "cutting_board");

	public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
		BRIDGED_OUTPUTS.clear();
		EXCLUSIVE_KEYS.clear();
		BridgeRegistry.clearBySource(BRIDGE_SOURCE);

		Item workstationItem = ForgeRegistries.ITEMS.getValue(FD_CUTTING_BOARD_ID);
		ItemStack workstationStack = workstationItem == null ? ItemStack.EMPTY : new ItemStack(workstationItem);

		Item cuttingboardItem = ForgeRegistries.ITEMS.getValue(CUTTINGBOARD_ITEM_ID);
		if (cuttingboardItem == null)
			return;
		ItemStack cbProbe = new ItemStack(cuttingboardItem);

		TagKey<Item> knivesTag = TagKey.create(Registries.ITEM, KNIVES_TAG_ID);
		Ingredient knifeIngredient = Ingredient.of(knivesTag);

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

			boolean hasCuttingboard = false;
			for (Ingredient ing : original) {
				if (ing != null && ing != Ingredient.EMPTY && ing.test(cbProbe)) {
					hasCuttingboard = true;
					break;
				}
			}
			if (!hasCuttingboard)
				continue;

			ItemStack result = recipe.getResultItem(registries);
			if (result.isEmpty())
				continue;
			if (!result.getItem().isEdible())
				continue;

			NonNullList<Ingredient> swapped = NonNullList.createWithCapacity(original.size());
			for (Ingredient ing : original) {
				if (ing != null && ing != Ingredient.EMPTY && ing.test(cbProbe)) {
					swapped.add(knifeIngredient);
				} else {
					swapped.add(ing == null ? Ingredient.EMPTY : ing);
				}
			}

			ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(result.getItem());
			if (outputId == null)
				continue;

			Collection<?> existing = CookingRegistry.getFoodRecipes(outputId);
			boolean outputHadPriorRecipe = !existing.isEmpty();

			ResourceLocation bridgeId = new ResourceLocation(BRIDGE_NAMESPACE,
					BRIDGE_PATH_PREFIX + recipe.getId().getNamespace() + "_" + recipe.getId().getPath());
			ToolBridgeShapelessRecipe bridge = new ToolBridgeShapelessRecipe(bridgeId, "", CraftingBookCategory.MISC,
					result.copy(), swapped, s -> s.is(knivesTag),
					new ResourceLocation("farmersdelight", "netherite_knife"));

			CookingRegistry.addFoodRecipe(bridge, registries);
			BridgeRegistry.register(new BridgeRegistry.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(swapped),
					result.copy(), workstationStack.copy()));
			BRIDGED_OUTPUTS.add(outputId);
			if (!outputHadPriorRecipe) {
				EXCLUSIVE_KEYS.add(outputId);
			}
		}
	}

	public static void recordStrippedRecipe(ResourceLocation id) {
		STRIPPED_RECIPE_IDS.add(id);
	}

	public static void clearStrippedRecipes() {
		STRIPPED_RECIPE_IDS.clear();
	}

	public static ResourceLocation cuttingboardItemId() {
		return CUTTINGBOARD_ITEM_ID;
	}
}
