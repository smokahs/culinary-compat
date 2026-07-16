package io.github.smokahs.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import io.github.smokahs.culinarycompat.CulinaryCompat;
import io.github.smokahs.culinarycompat.bridges.Bridges;
import io.github.smokahs.culinarycompat.registry.Recipes;

public final class Croptopia {
	public static final String CROPTOPIA_MODID = "croptopia";
	public static final ResourceLocation FOOD_PRESS_ITEM = new ResourceLocation(CROPTOPIA_MODID, "food_press");
	public static final ResourceLocation MORTAR_ITEM = new ResourceLocation(CROPTOPIA_MODID, "mortar_and_pestle");

	private static final String BRIDGE_NAMESPACE = "culinarycompat";
	private static final String BRIDGE_PATH_PREFIX = "croptopia_bridge/";
	// FD present: croptopia knife recipes swap to the FD knives tag and show the
	// netherite knife, matching the Pam + FD cutting board bridges.
	private static final ResourceLocation KNIVES_TAG_ID = new ResourceLocation("forge", "tools/knives");
	private static final TagKey<Item> KNIVES_TAG = TagKey.create(Registries.ITEM, KNIVES_TAG_ID);
	private static final ResourceLocation FD_NETHERITE_KNIFE = new ResourceLocation("farmersdelight",
			"netherite_knife");

	private static final Set<ResourceLocation> FRYING_PAN_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> COOKING_POT_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> KNIFE_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> FOOD_PRESS_OUTPUTS = ConcurrentHashMap.newKeySet();
	private static final Set<ResourceLocation> MORTAR_OUTPUTS = ConcurrentHashMap.newKeySet();

	private Croptopia() {
	}

	private enum Tool {
		FRYING_PAN("croptopia:frying_pan", "croptopia_frying_pan", "farmersdelight:skillet"), COOKING_POT(
				"croptopia:cooking_pot", "croptopia_cooking_pot",
				"farmersdelight:cooking_pot"), KNIFE("croptopia:knife", "croptopia_knife",
						"farmersdelight:cutting_board"), FOOD_PRESS("croptopia:food_press", "croptopia_food_press",
								"croptopia:food_press"), MORTAR("croptopia:mortar_and_pestle", "croptopia_mortar",
										"croptopia:mortar_and_pestle");

		final ResourceLocation itemId;
		final String source;
		final ResourceLocation workstationId;

		Tool(String itemId, String source, String workstationId) {
			this.itemId = new ResourceLocation(itemId);
			this.source = source;
			this.workstationId = new ResourceLocation(workstationId);
		}

		Set<ResourceLocation> outputs() {
			return switch (this) {
				case FRYING_PAN -> FRYING_PAN_OUTPUTS;
				case COOKING_POT -> COOKING_POT_OUTPUTS;
				case KNIFE -> KNIFE_OUTPUTS;
				case FOOD_PRESS -> FOOD_PRESS_OUTPUTS;
				case MORTAR -> MORTAR_OUTPUTS;
			};
		}
	}

	public static Set<ResourceLocation> getFryingPanOutputs() {
		return Collections.unmodifiableSet(FRYING_PAN_OUTPUTS);
	}

	public static Set<ResourceLocation> getCookingPotOutputs() {
		return Collections.unmodifiableSet(COOKING_POT_OUTPUTS);
	}

	public static Set<ResourceLocation> getKnifeOutputs() {
		return Collections.unmodifiableSet(KNIFE_OUTPUTS);
	}

	public static Set<ResourceLocation> getFoodPressOutputs() {
		return Collections.unmodifiableSet(FOOD_PRESS_OUTPUTS);
	}

	public static Set<ResourceLocation> getMortarOutputs() {
		return Collections.unmodifiableSet(MORTAR_OUTPUTS);
	}

	public static final class Stripper {
		private record Template(ResourceLocation bridgeId, String source, NonNullList<Ingredient> food,
				ItemStack result, ItemStack workstation, ResourceLocation outputId, EnumSet<Tool> used,
				boolean knifeSwap) {
		}

		private static volatile List<Template> templates = List.of();

		private Stripper() {
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onServerStarted(ServerStartedEvent event) {
			run(event.getServer());
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onDatapackSync(OnDatapackSyncEvent event) {
			MinecraftServer server = event.getPlayerList().getServer();
			if (server != null) {
				run(server);
			}
			net.minecraft.server.level.ServerPlayer player = event.getPlayer();
			if (player != null) {
				io.github.smokahs.culinarycompat.network.Network.sendBridges(player);
			} else {
				io.github.smokahs.culinarycompat.network.Network.sendBridgesToAll();
			}
		}

		private static void run(MinecraftServer server) {
			if (!ModList.get().isLoaded(CROPTOPIA_MODID) || !ModList.get().isLoaded(CFB.CFB_MODID)) {
				return;
			}

			RecipeManager rm = server.getRecipeManager();
			RegistryAccess registries = server.registryAccess();

			boolean fdPresent = ModList.get().isLoaded(CFB.FD_MODID);
			Ingredient fdKnife = Ingredient.of(KNIVES_TAG);

			List<Template> found = new ArrayList<>();
			Set<ResourceLocation> toStrip = new HashSet<>();
			for (Recipe<?> recipe : rm.getRecipes()) {
				if (recipe.getType() != RecipeType.CRAFTING || !(recipe instanceof CraftingRecipe)) {
					continue;
				}
				if (!CROPTOPIA_MODID.equals(recipe.getId().getNamespace())) {
					continue;
				}

				NonNullList<Ingredient> ings = recipe.getIngredients();
				if (ings.isEmpty()) {
					continue;
				}

				EnumSet<Tool> used = EnumSet.noneOf(Tool.class);
				NonNullList<Ingredient> food = NonNullList.create();
				for (Ingredient ing : ings) {
					if (ing == null || ing == Ingredient.EMPTY) {
						continue;
					}
					Tool tool = toolOf(ing);
					if (tool != null) {
						used.add(tool);
						if (tool == Tool.KNIFE && fdPresent) {
							food.add(fdKnife);
						}
					} else {
						food.add(ing);
					}
				}
				if (food.isEmpty()) {
					continue;
				}

				ItemStack result = recipe.getResultItem(registries);
				if (result.isEmpty()) {
					continue;
				}
				ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(result.getItem());
				if (outputId == null) {
					continue;
				}
				if (fdPresent && Tool.KNIFE.itemId.equals(outputId)) {
					toStrip.add(recipe.getId());
					continue;
				}
				if (used.isEmpty()) {
					continue;
				}

				Tool primary = used.iterator().next();
				String source = primary.source;
				ItemStack workstation = stackOf(primary.workstationId);

				boolean knifeSwap = fdPresent && used.contains(Tool.KNIFE);
				ResourceLocation bridgeId = new ResourceLocation(BRIDGE_NAMESPACE,
						BRIDGE_PATH_PREFIX + recipe.getId().getPath());
				found.add(new Template(bridgeId, source, food, result.copy(), workstation.copy(), outputId, used,
						knifeSwap));
				toStrip.add(recipe.getId());
			}

			if (toStrip.isEmpty()) {
				CulinaryCompat.LOGGER.info("Croptopia kitchen bridge: no matching recipes");
				return;
			}

			templates = found;

			List<Recipe<?>> kept = new ArrayList<>();
			for (Recipe<?> recipe : rm.getRecipes()) {
				if (!toStrip.contains(recipe.getId())) {
					kept.add(recipe);
				}
			}
			rm.replaceRecipes(kept);

			registerBridges(registries);

			CulinaryCompat.LOGGER.info(
					"Croptopia kitchen bridge: moved {} tool recipes into the kitchen (pan={}, pot={}, knife={}, press={}, mortar={})",
					found.size(), FRYING_PAN_OUTPUTS.size(), COOKING_POT_OUTPUTS.size(), KNIFE_OUTPUTS.size(),
					FOOD_PRESS_OUTPUTS.size(), MORTAR_OUTPUTS.size());
		}

		public static void registerBridges(RegistryAccess registries) {
			clearState();
			for (Template t : templates) {
				ShapelessRecipe bridge;
				if (t.knifeSwap()) {
					bridge = new Recipes.ToolBridgeShapeless(t.bridgeId(), "", CraftingBookCategory.MISC,
							t.result().copy(), t.food(), s -> s.is(KNIVES_TAG), FD_NETHERITE_KNIFE);
				} else {
					bridge = new ShapelessRecipe(t.bridgeId(), "", CraftingBookCategory.MISC, t.result().copy(),
							t.food());
				}
				CookingRegistry.addFoodRecipe(bridge, registries);
				Bridges.register(new Bridges.Entry(t.source(), t.bridgeId(), new ArrayList<>(t.food()),
						t.result().copy(), t.workstation().copy()));
				for (Tool tool : t.used()) {
					tool.outputs().add(t.outputId());
				}
			}
		}

		private static void clearState() {
			FRYING_PAN_OUTPUTS.clear();
			COOKING_POT_OUTPUTS.clear();
			KNIFE_OUTPUTS.clear();
			FOOD_PRESS_OUTPUTS.clear();
			MORTAR_OUTPUTS.clear();
			for (Tool tool : Tool.values()) {
				Bridges.clearBySource(tool.source);
			}
		}

		private static Tool toolOf(Ingredient ing) {
			ItemStack[] items = ing.getItems();
			if (items.length != 1) {
				return null;
			}
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(items[0].getItem());
			if (id == null) {
				return null;
			}
			for (Tool tool : Tool.values()) {
				if (tool.itemId.equals(id)) {
					return tool;
				}
			}
			return null;
		}

		private static ItemStack stackOf(ResourceLocation id) {
			Item item = ForgeRegistries.ITEMS.getValue(id);
			return item == null ? ItemStack.EMPTY : new ItemStack(item);
		}
	}
}
