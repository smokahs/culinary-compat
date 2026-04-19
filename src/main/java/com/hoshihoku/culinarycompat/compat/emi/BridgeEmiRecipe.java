package com.hoshihoku.culinarycompat.compat.emi;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeKind;
import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

public class BridgeEmiRecipe implements EmiRecipe {
	private static final ResourceLocation CUTTING_BG = new ResourceLocation("farmersdelight",
			"textures/gui/jei/cutting_board.png");
	private static final ResourceLocation FD_INTERFACE = new ResourceLocation("farmersdelight",
			"textures/gui/cooking_pot.png");

	private final BridgeKind kind;
	private final BridgeRegistry.Entry entry;
	private final EmiRecipeCategory category;
	private final List<EmiIngredient> inputs;
	private final List<EmiIngredient> foodInputs;
	private final EmiIngredient toolInput;
	private final List<EmiStack> outputs;
	private final List<EmiIngredient> catalysts;

	public BridgeEmiRecipe(BridgeRegistry.Entry entry, EmiRecipeCategory category, BridgeKind kind) {
		this.entry = entry;
		this.category = category;
		this.kind = kind;

		ItemStack toolStack = resolveToolStack(kind);

		this.inputs = new ArrayList<>();
		this.foodInputs = new ArrayList<>();
		EmiIngredient tool = null;
		for (Ingredient ing : entry.inputs()) {
			if (ing == null || ing == Ingredient.EMPTY)
				continue;
			EmiIngredient emi = EmiIngredient.of(ing);
			this.inputs.add(emi);
			if (tool == null && !toolStack.isEmpty() && ing.test(toolStack)) {
				tool = emi;
			} else {
				this.foodInputs.add(emi);
			}
		}
		this.toolInput = tool;

		this.outputs = List.of(EmiStack.of(entry.output()));
		ItemStack ws = entry.workstation();
		this.catalysts = ws.isEmpty() ? List.of() : List.of(EmiStack.of(ws));
	}

	private static ItemStack resolveToolStack(BridgeKind kind) {
		ResourceLocation id = switch (kind) {
			case CUTTINGBOARD -> new ResourceLocation("farmersdelight", "netherite_knife");
			case SKILLET -> new ResourceLocation("farmersdelight", "skillet");
			case POT -> new ResourceLocation("farmersdelight", "cooking_pot");
			case BAKEWARE -> new ResourceLocation("culinarycompat", "bakeware");
			default -> null;
		};
		if (id == null)
			return ItemStack.EMPTY;
		Item item = ForgeRegistries.ITEMS.getValue(id);
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}
	@Override
	public ResourceLocation getId() {
		return entry.bridgeId();
	}
	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}
	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}
	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}
	@Override
	public boolean supportsRecipeTree() {
		return true;
	}

	@Override
	public int getDisplayWidth() {
		return kind == BridgeKind.CUTTINGBOARD ? 117 : 118;
	}

	@Override
	public int getDisplayHeight() {
		return kind == BridgeKind.CUTTINGBOARD ? 57 : 56;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		switch (kind) {
			case POT -> drawPot(widgets);
			case CUTTINGBOARD -> drawCutting(widgets);
			default -> drawShapeless(widgets);
		}
	}

	private void drawPot(WidgetHolder widgets) {
		for (int i = 0; i < Math.min(foodInputs.size(), 9); i++) {
			int col = i % 3, row = i / 3;
			widgets.addSlot(foodInputs.get(i), col * 18, row * 18);
		}
		widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
		widgets.addTexture(new EmiTexture(FD_INTERFACE, 176, 15, 24, 17, 24, 17, 256, 256), 60, 18);
		widgets.addSlot(outputs.get(0), 92, 14).large(true).recipeContext(this);
		widgets.addTexture(new EmiTexture(FD_INTERFACE, 176, 0, 17, 15, 17, 15, 256, 256), 97, 41);
	}

	private void drawCutting(WidgetHolder widgets) {
		widgets.addTexture(new EmiTexture(CUTTING_BG, 0, 0, 117, 57, 117, 57, 256, 256), 0, 0);

		if (toolInput != null) {
			widgets.addSlot(toolInput, 16, 8).drawBack(false);
		}
		if (!foodInputs.isEmpty()) {
			widgets.addSlot(foodInputs.get(0), 16, 27).drawBack(false);
		}
		widgets.addSlot(outputs.get(0), 86, 20).drawBack(false).recipeContext(this);
	}

	private void drawShapeless(WidgetHolder widgets) {
		for (int i = 0; i < Math.min(foodInputs.size(), 9); i++) {
			int col = i % 3, row = i / 3;
			widgets.addSlot(foodInputs.get(i), col * 18, row * 18);
		}
		widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
		widgets.addSlot(outputs.get(0), 92, 14).large(true).recipeContext(this);
		widgets.addTexture(new EmiTexture(FD_INTERFACE, 176, 0, 17, 15, 17, 15, 256, 256), 97, 41);
	}
}
