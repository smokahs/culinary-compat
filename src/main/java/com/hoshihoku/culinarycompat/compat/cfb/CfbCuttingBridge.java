package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

public final class CfbCuttingBridge {
	private static final ResourceLocation CUTTING_TYPE_ID = new ResourceLocation(CfbIntegration.FD_MODID, "cutting");
	private static final ResourceLocation KNIVES_TAG_ID = new ResourceLocation("forge", "tools/knives");
	private static final String BRIDGE_NAMESPACE = "culinarycompat";
	private static final String BRIDGE_PATH_PREFIX = "cutting_bridge/";

	private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();

	private CfbCuttingBridge() {
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

	private static final String BRIDGE_SOURCE = "fd_cutting";
	private static final ResourceLocation FD_CUTTING_BOARD_ID = new ResourceLocation(CfbIntegration.FD_MODID,
			"cutting_board");

	public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
		BRIDGED_OUTPUTS.clear();
		EXCLUSIVE_KEYS.clear();
		BridgeRegistry.clearBySource(BRIDGE_SOURCE);

		RecipeType<?> cuttingType = BuiltInRegistries.RECIPE_TYPE.get(CUTTING_TYPE_ID);
		if (cuttingType == null)
			return;

		Set<ResourceLocation> craftingOutputs = CraftingOutputIndex.collect(recipeManager, registries);

		net.minecraft.world.item.Item workstationItem = ForgeRegistries.ITEMS.getValue(FD_CUTTING_BOARD_ID);
		ItemStack workstationStack = workstationItem == null ? ItemStack.EMPTY : new ItemStack(workstationItem);

		TagKey<net.minecraft.world.item.Item> knivesTag = TagKey.create(Registries.ITEM, KNIVES_TAG_ID);
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
			ToolBridgeShapelessRecipe bridge = new ToolBridgeShapelessRecipe(bridgeId, "", CraftingBookCategory.MISC,
					primary.copy(), ings, s -> s.is(knivesTag),
					new ResourceLocation("farmersdelight", "netherite_knife"));

			CookingRegistry.addFoodRecipe(bridge, registries);
			BridgeRegistry.register(new BridgeRegistry.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(ings),
					primary.copy(), workstationStack.copy()));
			BRIDGED_OUTPUTS.add(outputId);
			if (!outputHadPriorRecipe) {
				EXCLUSIVE_KEYS.add(outputId);
			}
		}
	}
}
