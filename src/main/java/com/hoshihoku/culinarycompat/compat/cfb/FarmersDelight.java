package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.Bridges;
import com.hoshihoku.culinarycompat.registry.Recipes;

import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

public final class FarmersDelight {
	private FarmersDelight() {
	}

	public static final class Cutting {
		private static final ResourceLocation CUTTING_TYPE_ID = new ResourceLocation(CFB.FD_MODID, "cutting");
		private static final ResourceLocation KNIVES_TAG_ID = new ResourceLocation("forge", "tools/knives");
		private static final String BRIDGE_NAMESPACE = "culinarycompat";
		private static final String BRIDGE_PATH_PREFIX = "cutting_bridge/";
		private static final String BRIDGE_SOURCE = "fd_cutting";
		private static final ResourceLocation FD_CUTTING_BOARD_ID = new ResourceLocation(CFB.FD_MODID, "cutting_board");

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();

		private Cutting() {
		}

		public static Set<ResourceLocation> getBridgedOutputs() {
			return Collections.unmodifiableSet(BRIDGED_OUTPUTS);
		}

		public static Set<ResourceLocation> getExclusiveBridgeKeys() {
			return Collections.unmodifiableSet(EXCLUSIVE_KEYS);
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onServerStarted(ServerStartedEvent event) {
			MinecraftServer server = event.getServer();
			registerBridges(server.getRecipeManager(), server.registryAccess());
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onDatapackSync(OnDatapackSyncEvent event) {
			MinecraftServer server = event.getPlayerList().getServer();
			registerBridges(server.getRecipeManager(), server.registryAccess());
		}

		public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
			BRIDGED_OUTPUTS.clear();
			EXCLUSIVE_KEYS.clear();
			Bridges.clearBySource(BRIDGE_SOURCE);

			RecipeType<?> cuttingType = BuiltInRegistries.RECIPE_TYPE.get(CUTTING_TYPE_ID);
			if (cuttingType == null)
				return;

			Set<ResourceLocation> craftingOutputs = OutputIndex.collect(recipeManager, registries);

			Item workstationItem = ForgeRegistries.ITEMS.getValue(FD_CUTTING_BOARD_ID);
			ItemStack workstationStack = workstationItem == null ? ItemStack.EMPTY : new ItemStack(workstationItem);

			TagKey<Item> knivesTag = TagKey.create(Registries.ITEM, KNIVES_TAG_ID);
			Ingredient knifeIngredient = Ingredient.of(knivesTag);

			for (Recipe<?> recipe : recipeManager.getRecipes()) {
				if (recipe.getType() != cuttingType)
					continue;

				ItemStack primary = recipe.getResultItem(registries);
				if (primary.isEmpty())
					continue;
				if (!primary.getItem().isEdible())
					continue;

				if (recipe.getIngredients().isEmpty())
					continue;
				Ingredient input = recipe.getIngredients().get(0);
				if (input == null || input == Ingredient.EMPTY)
					continue;

				ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(primary.getItem());
				if (outputId == null)
					continue;

				if (craftingOutputs.contains(outputId))
					continue;

				Collection<?> existing = CookingRegistry.getFoodRecipes(outputId);
				boolean outputHadPriorRecipe = !existing.isEmpty();

				NonNullList<Ingredient> ings = NonNullList.of(Ingredient.EMPTY, input, knifeIngredient);
				ResourceLocation bridgeId = new ResourceLocation(BRIDGE_NAMESPACE,
						BRIDGE_PATH_PREFIX + recipe.getId().getNamespace() + "_" + recipe.getId().getPath());
				Recipes.ToolBridgeShapeless bridge = new Recipes.ToolBridgeShapeless(bridgeId, "",
						CraftingBookCategory.MISC, primary.copy(), ings, s -> s.is(knivesTag),
						new ResourceLocation("farmersdelight", "netherite_knife"));

				CookingRegistry.addFoodRecipe(bridge, registries);
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(ings), primary.copy(),
						workstationStack.copy()));
				BRIDGED_OUTPUTS.add(outputId);
				if (!outputHadPriorRecipe) {
					EXCLUSIVE_KEYS.add(outputId);
				}
			}
		}
	}

	public static final class Pot {
		private static final ResourceLocation FD_COOKING_POT_ID = new ResourceLocation("farmersdelight", "cooking_pot");
		private static final ResourceLocation FD_COOKING_TYPE_ID = new ResourceLocation("farmersdelight", "cooking");
		private static final String BRIDGE_NAMESPACE = "culinarycompat";
		private static final String BRIDGE_PATH_PREFIX = "fd_cooking_bridge/";
		private static final String BRIDGE_SOURCE = "fd_cooking";

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();

		private Pot() {
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
			Bridges.clearBySource(BRIDGE_SOURCE);

			Item fdPot = ForgeRegistries.ITEMS.getValue(FD_COOKING_POT_ID);
			if (fdPot == null)
				return;
			ItemStack workstationStack = new ItemStack(fdPot);

			RecipeType<CookingPotRecipe> cookingType = (RecipeType<CookingPotRecipe>) BuiltInRegistries.RECIPE_TYPE
					.get(FD_COOKING_TYPE_ID);
			if (cookingType == null)
				return;

			Set<ResourceLocation> craftingOutputs = OutputIndex.collect(recipeManager, registries);

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
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(ingredients), result.copy(),
						workstationStack.copy(), recipe.getExperience()));
				BRIDGED_OUTPUTS.add(outputId);
				if (!outputHadPriorRecipe) {
					EXCLUSIVE_KEYS.add(outputId);
				}
			}
		}
	}

	public static final class Campfire {
		private static final ResourceLocation CFB_OVEN_ID = new ResourceLocation("cookingforblockheads", "oven");
		private static final String BRIDGE_SOURCE = "campfire_oven";

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> HIDDEN_CAMPFIRE_RECIPE_IDS = ConcurrentHashMap.newKeySet();

		private Campfire() {
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
			Bridges.clearBySource(BRIDGE_SOURCE);

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

			Set<ResourceLocation> craftingOutputs = OutputIndex.collect(recipeManager, registries);

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
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, recipe.getId(), new ArrayList<>(ings), result.copy(),
						workstationStack.copy()));
				BRIDGED_OUTPUTS.add(outputId);
				HIDDEN_CAMPFIRE_RECIPE_IDS.add(recipe.getId());
			}
		}
	}

	static final class OutputIndex {
		private OutputIndex() {
		}

		static Set<ResourceLocation> collect(RecipeManager recipeManager, RegistryAccess registries) {
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
}
