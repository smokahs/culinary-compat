package io.github.smokahs.culinarycompat.registry;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import io.github.smokahs.culinarycompat.CulinaryCompat;
import io.github.smokahs.culinarycompat.recipe.MultiCutting;

public final class Recipes {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, CulinaryCompat.MODID);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE,
			CulinaryCompat.MODID);

	public static final RegistryObject<RecipeSerializer<MultiCutting>> MULTI_CUTTING_SERIALIZER = RECIPE_SERIALIZERS
			.register("multi_cutting", (Supplier<RecipeSerializer<MultiCutting>>) MultiCutting.Serializer::new);
	public static final RegistryObject<RecipeType<MultiCutting>> MULTI_CUTTING_TYPE = RECIPE_TYPES
			.register("multi_cutting", () -> new RecipeType<MultiCutting>() {
				@Override
				public String toString() {
					return CulinaryCompat.MODID + ":multi_cutting";
				}
			});

	private Recipes() {
	}

	public static class ToolBridgeShapeless extends ShapelessRecipe {
		private final Predicate<ItemStack> toolTest;
		private final ResourceLocation immuneItemId;

		public ToolBridgeShapeless(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result,
				NonNullList<Ingredient> ingredients, Predicate<ItemStack> toolTest, ResourceLocation immuneItemId) {
			super(id, group, category, result, ingredients);
			this.toolTest = toolTest;
			this.immuneItemId = immuneItemId;
		}

		@Override
		public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
			NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
			for (int i = 0; i < remaining.size(); i++) {
				ItemStack stack = inv.getItem(i);
				if (stack.isEmpty())
					continue;
				if (toolTest != null && toolTest.test(stack)) {
					remaining.set(i, preserveTool(stack));
				} else if (stack.hasCraftingRemainingItem()) {
					remaining.set(i, stack.getCraftingRemainingItem());
				}
			}
			return remaining;
		}

		private ItemStack preserveTool(ItemStack stack) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(copy.getItem());
			if ((immuneItemId != null && immuneItemId.equals(id)) || !copy.isDamageableItem()) {
				return copy;
			}
			int newDamage = copy.getDamageValue() + 1;
			if (newDamage >= copy.getMaxDamage()) {
				return ItemStack.EMPTY;
			}
			copy.setDamageValue(newDamage);
			return copy;
		}
	}
}
