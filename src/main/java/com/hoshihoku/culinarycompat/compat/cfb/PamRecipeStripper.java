package com.hoshihoku.culinarycompat.compat.cfb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import com.hoshihoku.culinarycompat.CulinaryCompat;

public final class PamRecipeStripper {
	private static final TagKey<Item> CB_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tool_cuttingboard"));
	private static final TagKey<Item> SK_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tool_skillet"));
	private static final TagKey<Item> PT_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tool_pot"));
	private static final TagKey<Item> BK_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("forge", "tool_bakeware"));

	private static final ResourceLocation CB_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "cuttingboarditem");
	private static final ResourceLocation SK_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "skilletitem");
	private static final ResourceLocation PT_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "potitem");
	private static final ResourceLocation BK_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "tool_bakeware");

	private PamRecipeStripper() {
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
		if (!ModList.get().isLoaded("farmersdelight") || !ModList.get().isLoaded(PamCuttingBridge.PAM_MODID)
				|| !ModList.get().isLoaded("cookingforblockheads")) {
			return;
		}

		RecipeManager rm = server.getRecipeManager();

		Set<Item> cbItems = tagItems(CB_TAG);
		Set<Item> skItems = tagItems(SK_TAG);
		Set<Item> ptItems = tagItems(PT_TAG);
		Set<Item> bkItems = tagItems(BK_TAG);

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

		PamCuttingBridge.setTemplates(cbTemplates);
		PamSkilletBridge.setTemplates(skTemplates);
		PamPotBridge.setTemplates(ptTemplates);
		PamBakewareBridge.setTemplates(bkTemplates);

		PamCuttingBridge.registerBridges(rm, server.registryAccess());
		PamSkilletBridge.registerBridges(rm, server.registryAccess());
		PamPotBridge.registerBridges(rm, server.registryAccess());
		PamBakewareBridge.registerBridges(rm, server.registryAccess());

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

	// 1=cb 2=sk 3=pt 4=bk 0=none. priority matches original mixin
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
