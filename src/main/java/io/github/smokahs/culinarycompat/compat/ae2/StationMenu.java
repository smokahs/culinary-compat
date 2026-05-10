package io.github.smokahs.culinarycompat.compat.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

// settings panel for the ME station: mode (AE_TO_CFB / CFB_TO_AE / BIDIRECTIONAL), priority, view mode (NORMAL / OFF)
public class StationMenu extends AbstractContainerMenu {

	public static final int DATA_MODE = 0;
	public static final int DATA_PRIORITY = 1;
	public static final int DATA_VIEW = 2;
	private static final int DATA_COUNT = 3;

	private final StationBlockEntity station;
	private final BlockPos pos;
	private final ContainerData data;

	public StationMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
		this(containerId, playerInv, buf.readBlockPos(), null, new SimpleContainerData(DATA_COUNT));
	}

	public StationMenu(int containerId, Inventory playerInv, StationBlockEntity station) {
		this(containerId, playerInv, station.getBlockPos(), station, makeServerData(station));
	}

	private StationMenu(int containerId, Inventory playerInv, BlockPos pos, StationBlockEntity station,
			ContainerData data) {
		super(Bridge.KITCHEN_STATION_MENU.get(), containerId);
		this.pos = pos;
		this.station = station;
		this.data = data;
		addDataSlots(data);
	}

	private static ContainerData makeServerData(StationBlockEntity station) {
		return new ContainerData() {
			@Override
			public int get(int idx) {
				return switch (idx) {
					case DATA_MODE -> station.getMode().ordinal();
					case DATA_PRIORITY -> station.getPriority();
					case DATA_VIEW -> station.getViewMode().ordinal();
					default -> 0;
				};
			}

			@Override
			public void set(int idx, int val) {
			}

			@Override
			public int getCount() {
				return DATA_COUNT;
			}
		};
	}

	public BlockPos getPos() {
		return pos;
	}

	public Mode getMode() {
		return Mode.values()[clamp(data.get(DATA_MODE), Mode.values().length)];
	}

	public int getPriority() {
		return data.get(DATA_PRIORITY);
	}

	public ViewMode getViewMode() {
		return ViewMode.values()[clamp(data.get(DATA_VIEW), ViewMode.values().length)];
	}

	private static int clamp(int v, int max) {
		return Math.max(0, Math.min(max - 1, v));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		if (player.level().isClientSide) {
			return true;
		}
		BlockEntity be = player.level().getBlockEntity(pos);
		if (!(be instanceof StationBlockEntity)) {
			return false;
		}
		return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
	}
}
