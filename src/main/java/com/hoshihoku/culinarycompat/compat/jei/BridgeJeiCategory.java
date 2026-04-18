package com.hoshihoku.culinarycompat.compat.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.bridges.BridgeKind;
import com.hoshihoku.culinarycompat.bridges.BridgeRegistry;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class BridgeJeiCategory implements IRecipeCategory<BridgeRegistry.Entry> {
	private static final ResourceLocation CUTTING_BG = new ResourceLocation("farmersdelight",
			"textures/gui/jei/cutting_board.png");
	private static final ResourceLocation FD_INTERFACE = new ResourceLocation("farmersdelight",
			"textures/gui/cooking_pot.png");

	private final ResourceLocation uid;
	private final BridgeKind kind;
	private final RecipeType<BridgeRegistry.Entry> type;
	private final Component title;
	private final IDrawable icon;
	private final IDrawable background;
	private final IDrawable potArrow;
	private final IDrawable shapelessArrow;
	private final IDrawable fireIcon;

	public BridgeJeiCategory(ResourceLocation uid, BridgeKind kind, Component title, IGuiHelper helper,
			ItemStack iconStack) {
		this.uid = uid;
		this.kind = kind;
		this.type = new RecipeType<>(uid, BridgeRegistry.Entry.class);
		this.title = title;
		this.icon = iconStack.isEmpty()
				? helper.createBlankDrawable(16, 16)
				: helper.createDrawableItemStack(iconStack);

		this.background = switch (kind) {
			case CUTTINGBOARD -> helper.createDrawable(CUTTING_BG, 0, 0, 117, 57);
			default -> helper.createBlankDrawable(118, 56);
		};

		this.potArrow = kind == BridgeKind.POT ? helper.createDrawable(FD_INTERFACE, 176, 15, 24, 17) : null;
		this.shapelessArrow = (kind != BridgeKind.POT && kind != BridgeKind.CUTTINGBOARD)
				? helper.getRecipeArrow()
				: null;
		this.fireIcon = kind != BridgeKind.CUTTINGBOARD ? helper.createDrawable(FD_INTERFACE, 176, 0, 17, 15) : null;
	}

	public RecipeType<BridgeRegistry.Entry> type() {
		return type;
	}

	@Override
	public RecipeType<BridgeRegistry.Entry> getRecipeType() {
		return type;
	}
	@Override
	public Component getTitle() {
		return title;
	}
	@Override
	public IDrawable getIcon() {
		return icon;
	}
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public ResourceLocation getRegistryName(BridgeRegistry.Entry recipe) {
		return recipe.bridgeId();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BridgeRegistry.Entry recipe, IFocusGroup focuses) {
		ItemStack toolStack = resolveToolStack(kind);
		List<Ingredient> food = new ArrayList<>();
		Ingredient tool = null;
		for (Ingredient ing : recipe.inputs()) {
			if (ing == null || ing == Ingredient.EMPTY)
				continue;
			if (tool == null && !toolStack.isEmpty() && ing.test(toolStack)) {
				tool = ing;
			} else {
				food.add(ing);
			}
		}

		switch (kind) {
			case POT -> layoutPot(builder, recipe, food);
			case CUTTINGBOARD -> layoutCutting(builder, recipe, food, tool);
			default -> layoutShapeless(builder, recipe, food);
		}
	}

	private void layoutPot(IRecipeLayoutBuilder builder, BridgeRegistry.Entry recipe, List<Ingredient> food) {
		for (int i = 0; i < Math.min(food.size(), 9); i++) {
			int col = i % 3, row = i / 3;
			builder.addSlot(RecipeIngredientRole.INPUT, col * 18 + 1, row * 18 + 1).setStandardSlotBackground()
					.addIngredients(food.get(i));
		}
		builder.addOutputSlot(97, 19).setOutputSlotBackground().addItemStack(recipe.output());
		builder.setShapeless(97, 0);
	}

	private void layoutCutting(IRecipeLayoutBuilder builder, BridgeRegistry.Entry recipe, List<Ingredient> food,
			Ingredient tool) {
		if (tool != null) {
			builder.addSlot(RecipeIngredientRole.CATALYST, 16, 8).addIngredients(tool);
		}
		if (!food.isEmpty()) {
			builder.addSlot(RecipeIngredientRole.INPUT, 16, 27).addIngredients(food.get(0));
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 20).addItemStack(recipe.output());
	}

	private void layoutShapeless(IRecipeLayoutBuilder builder, BridgeRegistry.Entry recipe, List<Ingredient> food) {
		for (int i = 0; i < Math.min(food.size(), 9); i++) {
			int col = i % 3, row = i / 3;
			builder.addSlot(RecipeIngredientRole.INPUT, col * 18 + 1, row * 18 + 1).setStandardSlotBackground()
					.addIngredients(food.get(i));
		}
		builder.addOutputSlot(97, 19).setOutputSlotBackground().addItemStack(recipe.output());
		builder.setShapeless(97, 0);
	}

	@Override
	public void draw(BridgeRegistry.Entry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
			double mouseX, double mouseY) {
		if (kind == BridgeKind.POT) {
			potArrow.draw(guiGraphics, 60, 18);
			fireIcon.draw(guiGraphics, 97, 41);
			return;
		}
		if (kind == BridgeKind.CUTTINGBOARD)
			return;
		shapelessArrow.draw(guiGraphics, 60, 18);
		fireIcon.draw(guiGraphics, 97, 41);
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
}
