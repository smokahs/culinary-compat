package io.github.smokahs.culinarycompat.compat.ae2;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import appeng.api.features.IGridLinkableHandler;

public class StationItem extends BlockItem {

	public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();
	public static final String TAG_ACCESS_POINT_POS = "accessPointPos";

	public StationItem(StationBlock block, Properties props) {
		super(block, props);
	}

	private static class LinkableHandler implements IGridLinkableHandler {
		@Override
		public boolean canLink(ItemStack stack) {
			return stack.getItem() instanceof StationItem;
		}

		@Override
		public void link(ItemStack itemStack, GlobalPos pos) {
			GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result()
					.ifPresent(tagValue -> itemStack.getOrCreateTag().put(TAG_ACCESS_POINT_POS, tagValue));
		}

		@Override
		public void unlink(ItemStack itemStack) {
			itemStack.removeTagKey(TAG_ACCESS_POINT_POS);
		}
	}
}
