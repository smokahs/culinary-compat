package com.hoshihoku.culinarycompat.compat.emi;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.bridges.Bridges;
import com.hoshihoku.culinarycompat.compat.cfb.FarmersDelight;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

@EmiEntrypoint
@OnlyIn(Dist.CLIENT)
public final class Plugin implements EmiPlugin {
	public static final Map<Bridges.Kind, EmiRecipeCategory> CATEGORIES = new EnumMap<>(Bridges.Kind.class);

	private static final String[] HIDDEN = {"pamhc2crops:tomatoitem", "pamhc2crops:tomatoseeditem",
			"pamhc2crops:onionitem", "pamhc2crops:onionseeditem", "pamhc2crops:cabbageitem",
			"pamhc2crops:cabbageseeditem", "pamhc2crops:riceitem", "pamhc2crops:riceseeditem",
			"pamhc2foodcore:bakewareitem", "pamhc2foodcore:doughitem"};

	@Override
	public void register(EmiRegistry registry) {
		hideStacks(registry);
		registerBridgeCategories(registry);
		hideBridgedCampfireRecipes(registry);
		hideFdCookingRecipes(registry);
	}

	private static void hideFdCookingRecipes(EmiRegistry registry) {
		ResourceLocation fdCooking = new ResourceLocation("farmersdelight", "cooking");
		try {
			registry.removeRecipes(r -> {
				dev.emi.emi.api.recipe.EmiRecipeCategory cat = r.getCategory();
				return cat != null && fdCooking.equals(cat.getId());
			});
		} catch (Exception e) {
			CulinaryCompat.LOGGER.error("EMI fd:cooking hide failed", e);
		}
	}

	private static void hideStacks(EmiRegistry registry) {
		Set<Item> hidden = new HashSet<>();
		for (String id : HIDDEN) {
			ResourceLocation rl = ResourceLocation.tryParse(id);
			if (rl == null)
				continue;
			Item item = ForgeRegistries.ITEMS.getValue(rl);
			if (item != null && item != Items.AIR)
				hidden.add(item);
		}
		if (hidden.isEmpty())
			return;
		try {
			registry.removeEmiStacks(stack -> hidden.contains(stack.getItemStack().getItem()));
		} catch (Exception e) {
			CulinaryCompat.LOGGER.error("EMI hide failed", e);
		}
	}

	private static void registerBridgeCategories(EmiRegistry registry) {
		if (Bridges.size() == 0)
			return;

		CATEGORIES.clear();
		ItemStack cuttingBoard = itemStack("farmersdelight", "cutting_board");
		ItemStack skillet = itemStack("farmersdelight", "skillet");
		ItemStack oven = itemStack("cookingforblockheads", "oven");
		ItemStack pot = itemStack("farmersdelight", "cooking_pot");
		ItemStack bakeware = new ItemStack(com.hoshihoku.culinarycompat.registry.Items.BAKEWARE.get());

		registerCategory(registry, Bridges.Kind.CUTTINGBOARD, cuttingBoard);
		registerCategory(registry, Bridges.Kind.SKILLET, skillet);
		registerCategory(registry, Bridges.Kind.OVEN, oven);
		registerCategory(registry, Bridges.Kind.POT, pot);
		registerCategory(registry, Bridges.Kind.BAKEWARE, bakeware);

		ItemStack cookingTable = itemStack("cookingforblockheads", "cooking_table");
		if (!cookingTable.isEmpty()) {
			EmiStack ctStack = EmiStack.of(cookingTable);
			for (EmiRecipeCategory cat : CATEGORIES.values()) {
				registry.addWorkstation(cat, ctStack);
			}
		}
		if (!cuttingBoard.isEmpty()) {
			registry.addWorkstation(CATEGORIES.get(Bridges.Kind.CUTTINGBOARD), EmiStack.of(cuttingBoard));
		}
		if (!skillet.isEmpty()) {
			registry.addWorkstation(CATEGORIES.get(Bridges.Kind.SKILLET), EmiStack.of(skillet));
		}
		if (!oven.isEmpty()) {
			registry.addWorkstation(CATEGORIES.get(Bridges.Kind.OVEN), EmiStack.of(oven));
		}
		if (!pot.isEmpty()) {
			registry.addWorkstation(CATEGORIES.get(Bridges.Kind.POT), EmiStack.of(pot));
		}
		if (!bakeware.isEmpty()) {
			registry.addWorkstation(CATEGORIES.get(Bridges.Kind.BAKEWARE), EmiStack.of(bakeware));
		}

		for (Bridges.Entry entry : Bridges.getAll()) {
			Bridges.Kind kind = Bridges.Kind.fromSource(entry.source());
			if (kind == null)
				continue;
			EmiRecipeCategory cat = CATEGORIES.get(kind);
			if (cat == null)
				continue;
			registry.addRecipe(new Recipe(entry, cat, kind));
		}
	}

	private static void registerCategory(EmiRegistry registry, Bridges.Kind kind, ItemStack iconStack) {
		ResourceLocation id = new ResourceLocation(CulinaryCompat.MODID, "kitchen_bridge_" + kind.path);
		EmiStack icon = iconStack.isEmpty() ? EmiStack.of(new ItemStack(Items.CRAFTING_TABLE)) : EmiStack.of(iconStack);
		EmiRecipeCategory cat = new EmiRecipeCategory(id, icon);
		registry.addCategory(cat);
		CATEGORIES.put(kind, cat);
	}

	private static void hideBridgedCampfireRecipes(EmiRegistry registry) {
		Set<ResourceLocation> ids = new HashSet<>(FarmersDelight.Campfire.getHiddenCampfireRecipeIds());
		if (ids.isEmpty())
			return;
		try {
			registry.removeRecipes(r -> {
				ResourceLocation rid = r.getId();
				return rid != null && ids.contains(rid);
			});
		} catch (Exception e) {
			CulinaryCompat.LOGGER.error("EMI campfire hide failed", e);
		}
	}

	private static ItemStack itemStack(String ns, String path) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ns, path));
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}
}
