package io.github.smokahs.culinarycompat.compat.ae2.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// corner button

public class TabButton extends Button {

	private static final int BORDER_LIGHT = 0xFFFFFFFF;
	private static final int BORDER_DARK = 0xFF555555;
	private static final int BODY = 0xFFC6C6C6;
	private static final int BODY_HOVER = 0xFFDADADA;

	private final ItemStack icon;

	public TabButton(int x, int y, int w, int h, ItemStack icon, Component tooltip, OnPress onPress) {
		super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
		this.icon = icon;
		setTooltip(Tooltip.create(tooltip));
	}

	@Override
	protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		int x = getX();
		int y = getY();
		int w = width;
		int h = height;
		int body = isHoveredOrFocused() ? BODY_HOVER : BODY;
		g.fill(x, y, x + w, y + h, body);
		g.fill(x, y, x + w, y + 1, BORDER_LIGHT);
		g.fill(x + w - 1, y, x + w, y + h, BORDER_DARK);
		g.fill(x, y, x + 1, y + h, BORDER_LIGHT);
		g.renderItem(icon, x + (w - 16) / 2, y + (h - 16) / 2);
	}
}
