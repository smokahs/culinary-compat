package com.hoshihoku.culinarycompat.compat.cfb.bake;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.hoshihoku.culinarycompat.network.BakePhase;

public final class PendingBake {
	public BakePhase phase;
	public ResourceLocation outputId;
	public ItemStack output;
	public List<ItemStack> ingredients;
	public long startTick;

	public PendingBake(BakePhase phase, ResourceLocation outputId) {
		this.phase = phase;
		this.outputId = outputId;
		this.output = ItemStack.EMPTY;
		this.ingredients = List.of();
		this.startTick = 0L;
	}
}
