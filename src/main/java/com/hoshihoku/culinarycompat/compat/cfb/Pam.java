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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.bridges.Bridges;
import com.hoshihoku.culinarycompat.registry.Items;
import com.hoshihoku.culinarycompat.registry.Recipes;

public final class Pam {
	public static final String PAM_MODID = "pamhc2foodcore";

	private Pam() {
	}

	public static final class Bakeware {
		public static final ResourceLocation PAM_BAKEWARE_ITEM_ID = new ResourceLocation(PAM_MODID, "bakewareitem");
		private static final String BRIDGE_NAMESPACE = "culinarycompat";
		private static final String BRIDGE_PATH_PREFIX = "pam_bakeware_bridge/";
		private static final String BRIDGE_SOURCE = "pam_bakeware";

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
		private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

		private Bakeware() {
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
			Bridges.clearBySource(BRIDGE_SOURCE);

			Item ourBakeware = Items.BAKEWARE.get();
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
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(stripped), result.copy(),
						workstationStack.copy()));
				BRIDGED_OUTPUTS.add(outputId);
				if (!outputHadPriorRecipe) {
					EXCLUSIVE_KEYS.add(outputId);
				}
			}
			CulinaryCompat.LOGGER.info("Pam bakeware bridge: registered {} recipes", BRIDGED_OUTPUTS.size());
		}
	}

	public static final class Cutting {
		private static final ResourceLocation CUTTINGBOARD_TAG_ID = new ResourceLocation("forge", "tool_cuttingboard");
		private static final ResourceLocation KNIVES_TAG_ID = new ResourceLocation("forge", "tools/knives");
		private static final ResourceLocation CUTTINGBOARD_ITEM_ID = new ResourceLocation(PAM_MODID,
				"cuttingboarditem");
		private static final String BRIDGE_NAMESPACE = "culinarycompat";
		private static final String BRIDGE_PATH_PREFIX = "pam_cutting_bridge/";
		private static final String BRIDGE_SOURCE = "pam_cuttingboard";
		private static final ResourceLocation FD_CUTTING_BOARD_ID = new ResourceLocation("farmersdelight",
				"cutting_board");

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
		private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

		private Cutting() {
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

		public static ResourceLocation cuttingboardItemId() {
			return CUTTINGBOARD_ITEM_ID;
		}

		public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
			BRIDGED_OUTPUTS.clear();
			EXCLUSIVE_KEYS.clear();
			Bridges.clearBySource(BRIDGE_SOURCE);

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
				Recipes.ToolBridgeShapeless bridge = new Recipes.ToolBridgeShapeless(bridgeId, "",
						CraftingBookCategory.MISC, result.copy(), swapped, s -> s.is(knivesTag),
						new ResourceLocation("farmersdelight", "netherite_knife"));

				CookingRegistry.addFoodRecipe(bridge, registries);
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(swapped), result.copy(),
						workstationStack.copy()));
				BRIDGED_OUTPUTS.add(outputId);
				if (!outputHadPriorRecipe) {
					EXCLUSIVE_KEYS.add(outputId);
				}
			}
		}
	}

	public static final class Pot {
		public static final ResourceLocation POT_ITEM_ID = new ResourceLocation(PAM_MODID, "potitem");
		private static final ResourceLocation FD_COOKING_POT_ID = new ResourceLocation("farmersdelight", "cooking_pot");
		private static final String BRIDGE_NAMESPACE = "culinarycompat";
		private static final String BRIDGE_PATH_PREFIX = "pam_pot_bridge/";
		private static final String BRIDGE_SOURCE = "pam_pot";

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
		private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

		private Pot() {
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
			Bridges.clearBySource(BRIDGE_SOURCE);

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
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(stripped), result.copy(),
						workstationStack.copy()));
				BRIDGED_OUTPUTS.add(outputId);
				if (!outputHadPriorRecipe) {
					EXCLUSIVE_KEYS.add(outputId);
				}
			}
		}
	}

	public static final class Skillet {
		public static final ResourceLocation SKILLET_ITEM_ID = new ResourceLocation(PAM_MODID, "skilletitem");
		private static final ResourceLocation FD_SKILLET_ID = new ResourceLocation("farmersdelight", "skillet");
		private static final String BRIDGE_NAMESPACE = "culinarycompat";
		private static final String BRIDGE_PATH_PREFIX = "pam_skillet_bridge/";
		private static final String BRIDGE_SOURCE = "pam_skillet";

		private static final Set<ResourceLocation> BRIDGED_OUTPUTS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> EXCLUSIVE_KEYS = ConcurrentHashMap.newKeySet();
		private static final Set<ResourceLocation> STRIPPED_RECIPE_IDS = ConcurrentHashMap.newKeySet();
		private static final List<Recipe<?>> TEMPLATES = Collections.synchronizedList(new ArrayList<>());

		private Skillet() {
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

		public static ResourceLocation skilletItemId() {
			return SKILLET_ITEM_ID;
		}

		public static void registerBridges(RecipeManager recipeManager, RegistryAccess registries) {
			BRIDGED_OUTPUTS.clear();
			EXCLUSIVE_KEYS.clear();
			Bridges.clearBySource(BRIDGE_SOURCE);

			Item fdSkillet = ForgeRegistries.ITEMS.getValue(FD_SKILLET_ID);
			if (fdSkillet == null)
				return;
			ItemStack workstationStack = new ItemStack(fdSkillet);

			Item pamSkillet = ForgeRegistries.ITEMS.getValue(SKILLET_ITEM_ID);
			if (pamSkillet == null)
				return;
			ItemStack pamSkilletProbe = new ItemStack(pamSkillet);

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

				boolean hasSkillet = false;
				for (Ingredient ing : original) {
					if (ing != null && ing != Ingredient.EMPTY && ing.test(pamSkilletProbe)) {
						hasSkillet = true;
						break;
					}
				}
				if (!hasSkillet)
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
					if (ing.test(pamSkilletProbe))
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
				Bridges.register(new Bridges.Entry(BRIDGE_SOURCE, bridgeId, new ArrayList<>(stripped), result.copy(),
						workstationStack.copy()));
				BRIDGED_OUTPUTS.add(outputId);
				if (!outputHadPriorRecipe) {
					EXCLUSIVE_KEYS.add(outputId);
				}
			}
		}
	}

	public static final class CreativeTabFilter {
		private static final ResourceLocation[] HIDDEN_ITEMS = {Cutting.cuttingboardItemId(), Skillet.skilletItemId(),
				Pot.potItemId(), new ResourceLocation(PAM_MODID, "bakewareitem"),
				new ResourceLocation(PAM_MODID, "doughitem")};

		private CreativeTabFilter() {
		}

		@SubscribeEvent
		public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
			Set<Item> hidden = new HashSet<>();
			for (ResourceLocation id : HIDDEN_ITEMS) {
				Item it = ForgeRegistries.ITEMS.getValue(id);
				if (it != null)
					hidden.add(it);
			}
			if (hidden.isEmpty())
				return;

			List<ItemStack> toRemove = new ArrayList<>();
			for (java.util.Map.Entry<ItemStack, ?> entry : event.getEntries()) {
				if (hidden.contains(entry.getKey().getItem()))
					toRemove.add(entry.getKey());
			}
			for (ItemStack stack : toRemove) {
				event.getEntries().remove(stack);
			}
		}
	}

	public static final class RecipeStripper {
		private static final TagKey<Item> CB_TAG = TagKey.create(Registries.ITEM,
				new ResourceLocation("forge", "tool_cuttingboard"));
		private static final TagKey<Item> SK_TAG = TagKey.create(Registries.ITEM,
				new ResourceLocation("forge", "tool_skillet"));
		private static final TagKey<Item> PT_TAG = TagKey.create(Registries.ITEM,
				new ResourceLocation("forge", "tool_pot"));
		private static final TagKey<Item> BK_TAG = TagKey.create(Registries.ITEM,
				new ResourceLocation("forge", "tool_bakeware"));

		private static final ResourceLocation CB_RECIPE_ID = new ResourceLocation(PAM_MODID, "cuttingboarditem");
		private static final ResourceLocation SK_RECIPE_ID = new ResourceLocation(PAM_MODID, "skilletitem");
		private static final ResourceLocation PT_RECIPE_ID = new ResourceLocation(PAM_MODID, "potitem");
		private static final ResourceLocation BK_RECIPE_ID = new ResourceLocation(PAM_MODID, "tool_bakeware");

		private RecipeStripper() {
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
		}

		private static void run(MinecraftServer server) {
			if (!ModList.get().isLoaded("farmersdelight") || !ModList.get().isLoaded(PAM_MODID)
					|| !ModList.get().isLoaded("cookingforblockheads")) {
				return;
			}

			RecipeManager rm = server.getRecipeManager();

			Set<Item> cbItems = tagItems(CB_TAG);
			Set<Item> skItems = tagItems(SK_TAG);
			Set<Item> ptItems = tagItems(PT_TAG);
			Set<Item> bkItems = tagItems(BK_TAG);
			// pam bakeware item is removed from forge:tool_bakeware tag for cfb gating, but
			// pam recipes still
			// reference it directly — re-add for classify so bakeware recipes get stripped
			// + bridged
			Item pamBakewareItem = ForgeRegistries.ITEMS.getValue(Bakeware.PAM_BAKEWARE_ITEM_ID);
			if (pamBakewareItem != null) {
				bkItems.add(pamBakewareItem);
			}

			CulinaryCompat.LOGGER.info("Pam recipe strip tag sizes: cb={}, sk={}, pt={}, bk={}", cbItems.size(),
					skItems.size(), ptItems.size(), bkItems.size());

			Set<ResourceLocation> toStrip = new HashSet<>();
			List<Recipe<?>> cbTemplates = new ArrayList<>();
			List<Recipe<?>> skTemplates = new ArrayList<>();
			List<Recipe<?>> ptTemplates = new ArrayList<>();
			List<Recipe<?>> bkTemplates = new ArrayList<>();

			for (Recipe<?> recipe : rm.getRecipes()) {
				ResourceLocation rid = recipe.getId();
				if (CB_RECIPE_ID.equals(rid) || SK_RECIPE_ID.equals(rid) || PT_RECIPE_ID.equals(rid)
						|| BK_RECIPE_ID.equals(rid)) {
					toStrip.add(rid);
					continue;
				}
				if (recipe.getType() != RecipeType.CRAFTING) {
					continue;
				}
				int matched = classify(recipe, cbItems, skItems, ptItems, bkItems);
				if (matched == 0) {
					continue;
				}
				toStrip.add(rid);
				switch (matched) {
					case 1 -> cbTemplates.add(recipe);
					case 2 -> skTemplates.add(recipe);
					case 3 -> ptTemplates.add(recipe);
					case 4 -> bkTemplates.add(recipe);
				}
			}

			if (toStrip.isEmpty()) {
				CulinaryCompat.LOGGER.info("Pam recipe strip: no matches");
				return;
			}

			List<Recipe<?>> kept = new ArrayList<>();
			for (Recipe<?> recipe : rm.getRecipes()) {
				if (!toStrip.contains(recipe.getId())) {
					kept.add(recipe);
				}
			}
			rm.replaceRecipes(kept);

			Cutting.setTemplates(cbTemplates);
			Skillet.setTemplates(skTemplates);
			Pot.setTemplates(ptTemplates);
			Bakeware.setTemplates(bkTemplates);

			Cutting.registerBridges(rm, server.registryAccess());
			Skillet.registerBridges(rm, server.registryAccess());
			Pot.registerBridges(rm, server.registryAccess());
			Bakeware.registerBridges(rm, server.registryAccess());

			CulinaryCompat.LOGGER.info(
					"Pam recipe strip: removed {} recipes ({} cutting, {} skillet, {} pot, {} bakeware templates)",
					toStrip.size(), cbTemplates.size(), skTemplates.size(), ptTemplates.size(), bkTemplates.size());
		}

		private static Set<Item> tagItems(TagKey<Item> tag) {
			ITag<Item> resolved = ForgeRegistries.ITEMS.tags().getTag(tag);
			Set<Item> items = new HashSet<>();
			for (Item it : resolved) {
				items.add(it);
			}
			return items;
		}

		// 1=cb 2=sk 3=pt 4=bk 0=none, priority matches original mixin
		private static int classify(Recipe<?> recipe, Set<Item> cb, Set<Item> sk, Set<Item> pt, Set<Item> bk) {
			for (Ingredient ing : recipe.getIngredients()) {
				if (ing == null || ing == Ingredient.EMPTY) {
					continue;
				}
				ItemStack[] stacks = ing.getItems();
				if (stacks.length == 0) {
					continue;
				}
				for (ItemStack stack : stacks) {
					Item it = stack.getItem();
					if (cb.contains(it)) {
						return 1;
					}
					if (sk.contains(it)) {
						return 2;
					}
					if (pt.contains(it)) {
						return 3;
					}
					if (bk.contains(it)) {
						return 4;
					}
				}
			}
			return 0;
		}
	}
}
