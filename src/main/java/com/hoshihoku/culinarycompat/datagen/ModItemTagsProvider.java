package com.hoshihoku.culinarycompat.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.hoshihoku.culinarycompat.CulinaryCompat;

import org.jetbrains.annotations.Nullable;

public final class ModItemTagsProvider extends ItemTagsProvider {
	public ModItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
			CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
			@Nullable ExistingFileHelper existingFileHelper) {
		super(packOutput, lookupProvider, blockTags, CulinaryCompat.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
	}
}
