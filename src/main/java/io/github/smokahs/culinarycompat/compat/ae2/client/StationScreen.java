package io.github.smokahs.culinarycompat.compat.ae2.client;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import io.github.smokahs.culinarycompat.compat.ae2.Mode;
import io.github.smokahs.culinarycompat.compat.ae2.StationMenu;
import io.github.smokahs.culinarycompat.compat.ae2.StationSettingsPacket;
import io.github.smokahs.culinarycompat.compat.ae2.StationSettingsPacket.Action;
import io.github.smokahs.culinarycompat.compat.ae2.ViewMode;
import io.github.smokahs.culinarycompat.network.Network;

// tried matching ae2 panels whatever
public class StationScreen extends AbstractContainerScreen<StationMenu> {

	private static final int BORDER_OUTER = 0xFF000000;
	private static final int BORDER_LIGHT = 0xFFFFFFFF;
	private static final int BORDER_DARK = 0xFF555555;
	private static final int PANEL_BODY = 0xFFC6C6C6;
	private static final int PANEL_DIVIDER = 0xFF8B8B8B;
	private static final int TEXT_TITLE = 0x404040;
	private static final int TEXT_LABEL = 0x404040;

	public StationScreen(StationMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
		this.imageWidth = 200;
		this.imageHeight = 70;
		this.inventoryLabelY = this.imageHeight; // hide the inventory label (no inventory)
	}

	@Override
	protected void init() {
		super.init();

		int x = leftPos;
		int y = topPos;

		// mode cycle
		addRenderableWidget(Button
				.builder(Component.literal("Cycle"), b -> sendAction(Action.SET_MODE, menu.getMode().cycle().ordinal()))
				.bounds(x + 124, y + 22, 64, 16).build());

		// view-mode cycle
		addRenderableWidget(Button
				.builder(Component.literal("Cycle"),
						b -> sendAction(Action.SET_VIEW, menu.getViewMode().cycle().ordinal()))
				.bounds(x + 124, y + 46, 64, 16).build());

		// wrench priority tab fills the title-bar height in the upper-right corner.
		// shares the panel's top + right outer edges, only carves itself with a left
		// bevel.
		addRenderableWidget(new TabButton(x + imageWidth - 22, y, 22, 18, wrenchIcon(), Component.literal("Priority"),
				b -> sendAction(Action.OPEN_PRIORITY, 0)));
	}

	private static ItemStack wrenchIcon() {
		return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("ae2", "certus_quartz_wrench")));
	}

	private void sendAction(Action action, int value) {
		Network.CHANNEL.sendToServer(new StationSettingsPacket(menu.getPos(), action, value));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		int w = imageWidth;
		int h = imageHeight;
		// outer black border (1px)
		graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, BORDER_OUTER);
		// body
		graphics.fill(x, y, x + w, y + h, PANEL_BODY);
		// top + left highlight bevel
		graphics.fill(x, y, x + w, y + 1, BORDER_LIGHT);
		graphics.fill(x, y, x + 1, y + h, BORDER_LIGHT);
		// bottom + right shadow bevel
		graphics.fill(x, y + h - 1, x + w, y + h, BORDER_DARK);
		graphics.fill(x + w - 1, y, x + w, y + h, BORDER_DARK);
		// title-bar divider line, full inner width so it reads as one continuous bar
		// underneath both the title text and the priority tab
		graphics.fill(x + 1, y + 18, x + w - 1, y + 19, PANEL_DIVIDER);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		// hover tooltip on the "Mode: ..." label
		Mode mode = menu.getMode();
		String modeLabel = "Mode: " + modeName(mode);
		if (overText(mouseX, mouseY, 8, 28, font.width(modeLabel))) {
			graphics.renderComponentTooltip(
					font, List.of(
							Component.literal("Refer to the ").withStyle(ChatFormatting.GRAY)
									.append(Component.literal("Head Chef's Journal").withStyle(ChatFormatting.GOLD))
									.append(Component.literal(" for the full details on Mode switching!")
											.withStyle(ChatFormatting.GRAY)),
							Component.empty(),
							Component.literal("Default Mode: ").withStyle(ChatFormatting.GRAY)
									.append(Component.literal("BIDIRECTIONAL").withStyle(ChatFormatting.DARK_GREEN))),
					mouseX, mouseY);
		}

		// hover tooltip on the "Screen: ..." label
		ViewMode view = menu.getViewMode();
		String screenLabel = "Screen: " + viewName(view);
		if (overText(mouseX, mouseY, 8, 52, font.width(screenLabel))) {
			graphics.renderComponentTooltip(font,
					List.of(Component.literal("Screen modes are purely cosmetic and don't affect functionality!")
							.withStyle(ChatFormatting.GRAY)),
					mouseX, mouseY);
		}
	}

	private boolean overText(int mouseX, int mouseY, int relX, int relY, int textWidth) {
		int x = leftPos + relX;
		int y = topPos + relY;
		return mouseX >= x && mouseX < x + textWidth && mouseY >= y && mouseY < y + font.lineHeight;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// title
		graphics.drawString(font, this.title, 8, 7, TEXT_TITLE, false);

		// row 1: mode
		Mode mode = menu.getMode();
		graphics.drawString(font, Component.literal("Mode: ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(modeName(mode)).withStyle(modeColor(mode))), 8, 28, TEXT_LABEL, false);

		// row 2: view-mode
		ViewMode view = menu.getViewMode();
		graphics.drawString(font, Component.literal("Screen: ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(viewName(view)).withStyle(viewColor(view))), 8, 52, TEXT_LABEL, false);
	}

	private static String modeName(Mode mode) {
		return switch (mode) {
			case AE_TO_CFB -> "ME → Kitchen";
			case CFB_TO_AE -> "Kitchen → ME";
			case BIDIRECTIONAL -> "BIDIRECTIONAL";
		};
	}

	private static String viewName(ViewMode view) {
		return switch (view) {
			case NORMAL -> "On";
			case OFF -> "Off";
		};
	}

	private static ChatFormatting modeColor(Mode mode) {
		return switch (mode) {
			case AE_TO_CFB -> ChatFormatting.DARK_AQUA;
			case CFB_TO_AE -> ChatFormatting.GOLD;
			case BIDIRECTIONAL -> ChatFormatting.DARK_GREEN;
		};
	}

	private static ChatFormatting viewColor(ViewMode view) {
		return switch (view) {
			case NORMAL -> ChatFormatting.DARK_GREEN;
			case OFF -> ChatFormatting.BLACK;
		};
	}
}
