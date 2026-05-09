package io.github.smokahs.culinarycompat.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.github.smokahs.culinarycompat.registry.Recipes;

public class MultiCutting implements Recipe<RecipeWrapper> {
	public static final int MAX_INPUTS = 9;

	private final ResourceLocation id;
	private final NonNullList<Ingredient> ingredients;
	private final ItemStack result;

	public MultiCutting(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result) {
		this.id = id;
		this.ingredients = ingredients;
		this.result = result;
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level level) {
		List<ItemStack> stored = new ArrayList<>();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (!s.isEmpty()) {
				stored.add(s);
			}
		}
		if (stored.size() != ingredients.size()) {
			return false;
		}
		boolean[] used = new boolean[stored.size()];
		for (Ingredient ing : ingredients) {
			int found = -1;
			for (int j = 0; j < stored.size(); j++) {
				if (!used[j] && ing.test(stored.get(j))) {
					found = j;
					break;
				}
			}
			if (found < 0) {
				return false;
			}
			used[found] = true;
		}
		return true;
	}

	@Override
	public ItemStack assemble(RecipeWrapper inv, RegistryAccess registries) {
		return result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= ingredients.size();
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registries) {
		return result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Recipes.MULTI_CUTTING_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return Recipes.MULTI_CUTTING_TYPE.get();
	}

	public static final class Serializer implements RecipeSerializer<MultiCutting> {
		@Override
		public MultiCutting fromJson(ResourceLocation id, JsonObject json) {
			JsonArray arr = GsonHelper.getAsJsonArray(json, "ingredients");
			NonNullList<Ingredient> ings = NonNullList.create();
			for (int i = 0; i < arr.size(); i++) {
				Ingredient ing = Ingredient.fromJson(arr.get(i));
				if (!ing.isEmpty()) {
					ings.add(ing);
				}
			}
			if (ings.isEmpty() || ings.size() > MAX_INPUTS) {
				throw new JsonParseException(
						"multi_cutting recipe must have 1-" + MAX_INPUTS + " ingredients (got " + ings.size() + ")");
			}
			ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
			return new MultiCutting(id, ings, result);
		}

		@Override
		public MultiCutting fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			int n = buf.readVarInt();
			NonNullList<Ingredient> ings = NonNullList.withSize(n, Ingredient.EMPTY);
			for (int i = 0; i < n; i++) {
				ings.set(i, Ingredient.fromNetwork(buf));
			}
			ItemStack result = buf.readItem();
			return new MultiCutting(id, ings, result);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, MultiCutting recipe) {
			buf.writeVarInt(recipe.ingredients.size());
			for (Ingredient ing : recipe.ingredients) {
				ing.toNetwork(buf);
			}
			buf.writeItem(recipe.result);
		}
	}

}
