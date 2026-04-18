package com.hoshihoku.culinarycompat.compat.jei;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.bridges.BridgeKind;
import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;
import com.hoshihoku.culinarycompat.compat.cfb.CfbCampfireBridge;
import com.hoshihoku.culinarycompat.registry.ModItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

@JeiPlugin
@OnlyIn(Dist.CLIENT)
public final class CulinaryCompatJeiPlugin implements IModPlugin {
	private static final ResourceLocation UID = new ResourceLocation(CulinaryCompat.MODID, "jei");

	private static final Map<BridgeKind, BridgeJeiCategory> CATEGORIES = new EnumMap<>(BridgeKind.class);

	private static final String[] HIDDEN = {"pamhc2crops:tomatoitem", "pamhc2crops:tomatoseeditem",
			"pamhc2crops:onionitem", "pamhc2crops:onionseeditem", "pamhc2crops:cabbageitem",
			"pamhc2crops:cabbageseeditem", "pamhc2crops:riceitem", "pamhc2crops:riceseeditem",
			"pamhc2foodcore:bakewareitem", "pamhc2foodcore:doughitem"};

	private static boolean emiLoaded() {
		return ModList.get().isLoaded("emi");
	}

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (emiLoaded())
			return;
		CATEGORIES.clear();
		ItemStack cuttingBoard = itemStack("farmersdelight", "cutting_board");
		ItemStack skillet = itemStack("farmersdelight", "skillet");
		ItemStack oven = itemStack("cookingforblockheads", "oven");
		ItemStack pot = itemStack("farmersdelight", "cooking_pot");
		ItemStack bakeware = new ItemStack(ModItems.BAKEWARE.get());
		registerCategory(registration, BridgeKind.CUTTINGBOARD, cuttingBoard);
		registerCategory(registration, BridgeKind.SKILLET, skillet);
		registerCategory(registration, BridgeKind.OVEN, oven);
		registerCategory(registration, BridgeKind.POT, pot);
		registerCategory(registration, BridgeKind.BAKEWARE, bakeware);
	}

	private static void registerCategory(IRecipeCategoryRegistration registration, BridgeKind kind,
			ItemStack iconStack) {
		ResourceLocation uid = new ResourceLocation(CulinaryCompat.MODID, "kitchen_bridge_" + kind.path);
		Component title = Component.translatable("emi.category.culinarycompat.kitchen_bridge_" + kind.path);
		BridgeJeiCategory cat = new BridgeJeiCategory(uid, kind, title, registration.getJeiHelpers().getGuiHelper(),
				iconStack);
		registration.addRecipeCategories(cat);
		CATEGORIES.put(kind, cat);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (emiLoaded())
			return;
		Map<BridgeKind, List<BridgeRegistry.Entry>> bucket = new EnumMap<>(BridgeKind.class);
		for (BridgeRegistry.Entry e : BridgeRegistry.getAll()) {
			BridgeKind k = BridgeKind.fromSource(e.source());
			if (k == null)
				continue;
			bucket.computeIfAbsent(k, key -> new ArrayList<>()).add(e);
		}
		for (Map.Entry<BridgeKind, List<BridgeRegistry.Entry>> en : bucket.entrySet()) {
			BridgeJeiCategory cat = CATEGORIES.get(en.getKey());
			if (cat == null)
				continue;
			registration.addRecipes(cat.type(), en.getValue());
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (emiLoaded())
			return;
		ItemStack cookingTable = itemStack("cookingforblockheads", "cooking_table");
		ItemStack cuttingBoard = itemStack("farmersdelight", "cutting_board");
		ItemStack skillet = itemStack("farmersdelight", "skillet");
		ItemStack oven = itemStack("cookingforblockheads", "oven");
		ItemStack pot = itemStack("farmersdelight", "cooking_pot");
		ItemStack bakeware = new ItemStack(ModItems.BAKEWARE.get());

		for (Map.Entry<BridgeKind, BridgeJeiCategory> ce : CATEGORIES.entrySet()) {
			BridgeJeiCategory cat = ce.getValue();
			if (!cookingTable.isEmpty()) {
				registration.addRecipeCatalyst(cookingTable, cat.type());
			}
		}
		BridgeJeiCategory cb = CATEGORIES.get(BridgeKind.CUTTINGBOARD);
		if (cb != null && !cuttingBoard.isEmpty()) {
			registration.addRecipeCatalyst(cuttingBoard, cb.type());
		}
		BridgeJeiCategory sk = CATEGORIES.get(BridgeKind.SKILLET);
		if (sk != null && !skillet.isEmpty()) {
			registration.addRecipeCatalyst(skillet, sk.type());
		}
		BridgeJeiCategory ov = CATEGORIES.get(BridgeKind.OVEN);
		if (ov != null && !oven.isEmpty()) {
			registration.addRecipeCatalyst(oven, ov.type());
		}
		BridgeJeiCategory po = CATEGORIES.get(BridgeKind.POT);
		if (po != null && !pot.isEmpty()) {
			registration.addRecipeCatalyst(pot, po.type());
		}
		BridgeJeiCategory bk = CATEGORIES.get(BridgeKind.BAKEWARE);
		if (bk != null && !bakeware.isEmpty()) {
			registration.addRecipeCatalyst(bakeware, bk.type());
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (emiLoaded())
			return;
		List<ItemStack> hide = new ArrayList<>(HIDDEN.length);
		for (String id : HIDDEN) {
			ResourceLocation rl = ResourceLocation.tryParse(id);
			if (rl == null)
				continue;
			Item item = ForgeRegistries.ITEMS.getValue(rl);
			if (item != null && item != Items.AIR)
				hide.add(new ItemStack(item));
		}
		if (!hide.isEmpty()) {
			try {
				runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hide);
			} catch (Exception e) {
				CulinaryCompat.LOGGER.error("JEI hide failed", e);
			}
		}
		Level level = Minecraft.getInstance().level;
		if (level != null) {
			mezz.jei.api.recipe.RecipeType<CookingPotRecipe> fdCooking = mezz.jei.api.recipe.RecipeType
					.create("farmersdelight", "cooking", CookingPotRecipe.class);
			try {
				List<CookingPotRecipe> fdCookingRecipes = new ArrayList<>(level.getRecipeManager().getAllRecipesFor(
						(RecipeType<CookingPotRecipe>) net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE
								.get(new ResourceLocation("farmersdelight", "cooking"))));
				if (!fdCookingRecipes.isEmpty()) {
					runtime.getRecipeManager().hideRecipes(fdCooking, fdCookingRecipes);
				}
			} catch (Exception e) {
				CulinaryCompat.LOGGER.error("JEI fd:cooking hide failed", e);
			}
		}
		Set<ResourceLocation> campfireIds = CfbCampfireBridge.getHiddenCampfireRecipeIds();
		if (!campfireIds.isEmpty() && level != null) {
			try {
				List<CampfireCookingRecipe> toHide = new ArrayList<>();
				for (CampfireCookingRecipe r : level.getRecipeManager().getAllRecipesFor(RecipeType.CAMPFIRE_COOKING)) {
					if (campfireIds.contains(r.getId()))
						toHide.add(r);
				}
				if (!toHide.isEmpty()) {
					runtime.getRecipeManager().hideRecipes(RecipeTypes.CAMPFIRE_COOKING, toHide);
				}
			} catch (Exception e) {
				CulinaryCompat.LOGGER.error("JEI campfire hide failed", e);
			}
		}
	}

	private static ItemStack itemStack(String ns, String path) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ns, path));
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}
}
