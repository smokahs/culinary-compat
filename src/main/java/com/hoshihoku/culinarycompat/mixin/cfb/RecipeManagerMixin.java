package com.hoshihoku.culinarycompat.mixin.cfb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.ModList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.compat.cfb.PamBakewareBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamCuttingBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamPotBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamSkilletBridge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
	private static final String CB_TAG = "forge:tool_cuttingboard";
	private static final String CB_SUBTAG_PREFIX = "forge:tool_cuttingboard/";
	private static final String CB_ITEM = "pamhc2foodcore:cuttingboarditem";
	private static final ResourceLocation CB_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "cuttingboarditem");

	private static final String SK_TAG = "forge:tool_skillet";
	private static final String SK_SUBTAG_PREFIX = "forge:tool_skillet/";
	private static final String SK_ITEM = "pamhc2foodcore:skilletitem";
	private static final ResourceLocation SK_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "skilletitem");

	private static final String PT_TAG = "forge:tool_pot";
	private static final String PT_SUBTAG_PREFIX = "forge:tool_pot/";
	private static final String PT_ITEM = "pamhc2foodcore:potitem";
	private static final ResourceLocation PT_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "potitem");

	private static final String BK_TAG = "forge:tool_bakeware";
	private static final String BK_SUBTAG_PREFIX = "forge:tool_bakeware/";
	private static final String BK_ITEM = "pamhc2foodcore:bakewareitem";
	private static final ResourceLocation BK_RECIPE_ID = new ResourceLocation("pamhc2foodcore", "tool_bakeware");

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
	private void culinarycompat$stripCuttingboardRecipes(Map<ResourceLocation, JsonElement> json,
			ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
		if (!ModList.get().isLoaded("farmersdelight") || !ModList.get().isLoaded(PamCuttingBridge.PAM_MODID)
				|| !ModList.get().isLoaded("cookingforblockheads")) {
			return;
		}

		Set<ResourceLocation> toStrip = new HashSet<>();
		Set<ResourceLocation> cbBridgeable = new HashSet<>();
		Set<ResourceLocation> skBridgeable = new HashSet<>();
		Set<ResourceLocation> ptBridgeable = new HashSet<>();
		Set<ResourceLocation> bkBridgeable = new HashSet<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : json.entrySet()) {
			ResourceLocation id = entry.getKey();
			if (CB_RECIPE_ID.equals(id) || SK_RECIPE_ID.equals(id) || PT_RECIPE_ID.equals(id)
					|| BK_RECIPE_ID.equals(id)) {
				toStrip.add(id);
				continue;
			}
			if (jsonReferencesAny(entry.getValue(), CB_TAG, CB_SUBTAG_PREFIX, CB_ITEM)) {
				toStrip.add(id);
				cbBridgeable.add(id);
				continue;
			}
			if (jsonReferencesAny(entry.getValue(), SK_TAG, SK_SUBTAG_PREFIX, SK_ITEM)) {
				toStrip.add(id);
				skBridgeable.add(id);
				continue;
			}
			if (jsonReferencesAny(entry.getValue(), PT_TAG, PT_SUBTAG_PREFIX, PT_ITEM)) {
				toStrip.add(id);
				ptBridgeable.add(id);
				continue;
			}
			if (jsonReferencesAny(entry.getValue(), BK_TAG, BK_SUBTAG_PREFIX, BK_ITEM)) {
				toStrip.add(id);
				bkBridgeable.add(id);
			}
		}
		if (toStrip.isEmpty())
			return;

		RecipeManager self = (RecipeManager) (Object) this;
		List<Recipe<?>> kept = new ArrayList<>();
		List<Recipe<?>> cbTemplates = new ArrayList<>();
		List<Recipe<?>> skTemplates = new ArrayList<>();
		List<Recipe<?>> ptTemplates = new ArrayList<>();
		List<Recipe<?>> bkTemplates = new ArrayList<>();
		for (Recipe<?> recipe : self.getRecipes()) {
			ResourceLocation rid = recipe.getId();
			if (toStrip.contains(rid)) {
				if (cbBridgeable.contains(rid)) {
					cbTemplates.add(recipe);
				} else if (skBridgeable.contains(rid)) {
					skTemplates.add(recipe);
				} else if (ptBridgeable.contains(rid)) {
					ptTemplates.add(recipe);
				} else if (bkBridgeable.contains(rid)) {
					bkTemplates.add(recipe);
				}
				continue;
			}
			kept.add(recipe);
		}
		PamCuttingBridge.setTemplates(cbTemplates);
		PamSkilletBridge.setTemplates(skTemplates);
		PamPotBridge.setTemplates(ptTemplates);
		PamBakewareBridge.setTemplates(bkTemplates);
		self.replaceRecipes(kept);
		CulinaryCompat.LOGGER.info(
				"Pam bridge strip: removed {} recipes ({} cutting, {} skillet, {} pot, {} bakeware templates)",
				toStrip.size(), cbTemplates.size(), skTemplates.size(), ptTemplates.size(), bkTemplates.size());
	}

	private static boolean jsonReferencesAny(JsonElement el, String tag, String subtagPrefix, String itemId) {
		if (!el.isJsonObject())
			return false;
		JsonObject obj = el.getAsJsonObject();
		JsonElement ings = obj.get("ingredients");
		if (ings != null && ings.isJsonArray()) {
			for (JsonElement ing : ings.getAsJsonArray()) {
				if (ingReferences(ing, tag, subtagPrefix, itemId))
					return true;
			}
		}
		JsonElement key = obj.get("key");
		if (key != null && key.isJsonObject()) {
			for (Map.Entry<String, JsonElement> k : key.getAsJsonObject().entrySet()) {
				if (ingReferences(k.getValue(), tag, subtagPrefix, itemId))
					return true;
			}
		}
		JsonElement ingredient = obj.get("ingredient");
		if (ingredient != null && ingReferences(ingredient, tag, subtagPrefix, itemId))
			return true;
		return false;
	}

	private static boolean ingReferences(JsonElement el, String tag, String subtagPrefix, String itemId) {
		if (el == null)
			return false;
		if (el.isJsonObject()) {
			JsonObject o = el.getAsJsonObject();
			if (o.has("tag")) {
				String t = o.get("tag").getAsString();
				if (tag.equals(t) || t.startsWith(subtagPrefix))
					return true;
			}
			if (o.has("item")) {
				String item = o.get("item").getAsString();
				if (itemId.equals(item))
					return true;
			}
		} else if (el.isJsonArray()) {
			for (JsonElement sub : el.getAsJsonArray()) {
				if (ingReferences(sub, tag, subtagPrefix, itemId))
					return true;
			}
		}
		return false;
	}
}
