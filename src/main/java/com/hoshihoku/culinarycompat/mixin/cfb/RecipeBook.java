package com.hoshihoku.culinarycompat.mixin.cfb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.blay09.mods.cookingforblockheads.api.RecipeStatus;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.blay09.mods.cookingforblockheads.menu.inventory.InventoryCraftBook;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.blay09.mods.cookingforblockheads.registry.FoodRecipeType;
import net.blay09.mods.cookingforblockheads.registry.recipe.FoodRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.collect.Multimap;
import com.hoshihoku.culinarycompat.compat.cfb.BakeState;
import com.hoshihoku.culinarycompat.compat.cfb.CFB;
import com.hoshihoku.culinarycompat.compat.cfb.FarmersDelight;
import com.hoshihoku.culinarycompat.compat.cfb.Pam;
import com.hoshihoku.culinarycompat.config.Configs;
import com.hoshihoku.culinarycompat.network.Network.BakePhase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeBookMenu.class, remap = false)
public abstract class RecipeBook {
	@Shadow(remap = false)
	private KitchenMultiBlock multiBlock;
	@Shadow(remap = false)
	private InventoryCraftBook craftBook;
	@Shadow(remap = false)
	private Player player;

	@Unique
	private static final ResourceLocation CULINARYCOMPAT$FD_CUTTING_BOARD = new ResourceLocation("farmersdelight",
			"cutting_board");
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$FD_SKILLET = new ResourceLocation("farmersdelight", "skillet");
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$FD_COOKING_POT = new ResourceLocation("farmersdelight",
			"cooking_pot");
	@Unique
	private static final ResourceLocation CULINARYCOMPAT$BAKEWARE = new ResourceLocation("culinarycompat", "bakeware");

	@Redirect(method = "findAndSendItemList", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;keySet()Ljava/util/Set;", remap = false), remap = false, require = 0)
	private Set<?> culinarycompat$filterKeys(Multimap<ResourceLocation, FoodRecipe> map) {
		Set<ResourceLocation> keys = map.keySet();
		Set<ResourceLocation> hidden = culinarycompat$hiddenKeysMissing();
		if (hidden.isEmpty())
			return keys;
		Set<ResourceLocation> filtered = new HashSet<>(keys);
		filtered.removeAll(hidden);
		return filtered;
	}

	@Redirect(method = "findAndSendRecipes", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/cookingforblockheads/registry/CookingRegistry;getFoodRecipes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Collection;", remap = false), remap = false, require = 0)
	private Collection<FoodRecipe> culinarycompat$filterRecipesForOutput(ItemStack outputItem) {
		Collection<FoodRecipe> col = CookingRegistry.getFoodRecipes(outputItem);
		if (col.isEmpty())
			return col;
		Set<ResourceLocation> hidden = culinarycompat$hiddenKeysMissing();
		if (hidden.isEmpty())
			return col;
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(outputItem.getItem());
		if (id != null && hidden.contains(id)) {
			return Collections.emptyList();
		}
		return col;
	}

	@Redirect(method = "findAndSendItemList", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/cookingforblockheads/registry/CookingRegistry;getRecipeStatus(Lnet/blay09/mods/cookingforblockheads/registry/recipe/FoodRecipe;Ljava/util/List;Z)Lnet/blay09/mods/cookingforblockheads/api/RecipeStatus;", remap = false), remap = false, require = 0)
	private RecipeStatus culinarycompat$wrapStatusList(FoodRecipe recipe, List<IKitchenItemProvider> inventories,
			boolean hasOven) {
		return culinarycompat$adjustStatus(CookingRegistry.getRecipeStatus(recipe, inventories, hasOven), recipe);
	}

	@Redirect(method = "findAndSendRecipes", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/cookingforblockheads/registry/CookingRegistry;getRecipeStatus(Lnet/blay09/mods/cookingforblockheads/registry/recipe/FoodRecipe;Ljava/util/List;Z)Lnet/blay09/mods/cookingforblockheads/api/RecipeStatus;", remap = false), remap = false, require = 0)
	private RecipeStatus culinarycompat$wrapStatusRecipes(FoodRecipe recipe, List<IKitchenItemProvider> inventories,
			boolean hasOven) {
		return culinarycompat$adjustStatus(CookingRegistry.getRecipeStatus(recipe, inventories, hasOven), recipe);
	}

	@Inject(method = "tryCraft", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void culinarycompat$blockTryCraft(ItemStack outputItem, FoodRecipeType recipeType,
			NonNullList<ItemStack> craftMatrix, boolean stack, CallbackInfo ci) {
		if (recipeType != FoodRecipeType.CRAFTING)
			return;
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(outputItem.getItem());
		if (id == null)
			return;
		if (culinarycompat$toolsMissingKeys().contains(id)) {
			ci.cancel();
			return;
		}
		if (!(player instanceof ServerPlayer sp))
			return;
		if (!Pam.Bakeware.getExclusiveBridgeKeys().contains(id))
			return;
		if (!Configs.Common.bakewareEnabled)
			return;

		UUID pid = sp.getUUID();
		BakeState.Pending pending = BakeState.Manager.get(pid);
		if (pending != null && pending.phase == BakePhase.BAKING) {
			ci.cancel();
			return;
		}
		if (pending != null && pending.phase == BakePhase.CONFIRM && id.equals(pending.outputId)) {
			java.util.List<ItemStack> snapshot = new ArrayList<>();
			for (ItemStack s : craftMatrix) {
				snapshot.add(s == null ? ItemStack.EMPTY : s.copy());
			}
			ItemStack result = craftBook.tryCraft(outputItem, craftMatrix, player, multiBlock);
			if (result.isEmpty()) {
				BakeState.Manager.clear(pid);
			} else {
				BakeState.Manager.setBaking(sp, id, result, snapshot);
			}
			ci.cancel();
			return;
		}
		BakeState.Manager.setConfirm(sp, id);
		ci.cancel();
	}

	@Unique
	private RecipeStatus culinarycompat$adjustStatus(RecipeStatus status, FoodRecipe recipe) {
		if (status != RecipeStatus.AVAILABLE)
			return status;
		ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(recipe.getOutputItem().getItem());
		if (outputId == null)
			return status;
		if (culinarycompat$toolsMissingKeys().contains(outputId)) {
			return RecipeStatus.MISSING_TOOLS;
		}
		return status;
	}

	@Unique
	private Set<ResourceLocation> culinarycompat$hiddenKeysMissing() {
		return Collections.emptySet();
	}

	@Unique
	private Set<ResourceLocation> culinarycompat$toolsMissingKeys() {
		Set<ResourceLocation> tools = new HashSet<>();
		if (!culinarycompat$memberPresent(CULINARYCOMPAT$FD_CUTTING_BOARD)) {
			tools.addAll(FarmersDelight.Cutting.getBridgedOutputs());
			tools.addAll(Pam.Cutting.getBridgedOutputs());
		}
		if (!culinarycompat$memberPresent(CULINARYCOMPAT$FD_SKILLET)) {
			tools.addAll(Pam.Skillet.getBridgedOutputs());
		}
		if (!culinarycompat$memberPresent(CULINARYCOMPAT$FD_COOKING_POT)) {
			tools.addAll(Pam.Pot.getBridgedOutputs());
			tools.addAll(FarmersDelight.Pot.getBridgedOutputs());
		}
		if (!culinarycompat$memberPresent(CULINARYCOMPAT$BAKEWARE)) {
			tools.addAll(Pam.Bakeware.getBridgedOutputs());
		}
		return tools;
	}

	@Unique
	private boolean culinarycompat$memberPresent(ResourceLocation id) {
		if (multiBlock == null)
			return false;
		Set<ResourceLocation> members = ((CFB.KitchenMemberView) multiBlock).culinarycompat$getMemberBlocks();
		return members.contains(id);
	}
}
