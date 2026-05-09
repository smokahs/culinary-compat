package com.hoshihoku.culinarycompat.datagen;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.hoshihoku.culinarycompat.CulinaryCompat;

import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
	private DataGenerators() {
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		BlockTags blockTags = new BlockTags(packOutput, lookupProvider, existingFileHelper);
		generator.addProvider(event.includeServer(), blockTags);
		generator.addProvider(event.includeServer(),
				new ItemTags(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
		generator.addProvider(event.includeServer(), new Recipes(packOutput));

		generator.addProvider(event.includeClient(), new Lang(packOutput));
	}

	static final class BlockTags extends BlockTagsProvider {
		BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
				@Nullable ExistingFileHelper existingFileHelper) {
			super(output, lookupProvider, CulinaryCompat.MODID, existingFileHelper);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
		}
	}

	static final class ItemTags extends ItemTagsProvider {
		ItemTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
				CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
				@Nullable ExistingFileHelper existingFileHelper) {
			super(packOutput, lookupProvider, blockTags, CulinaryCompat.MODID, existingFileHelper);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
		}
	}

	static final class Lang extends LanguageProvider {
		Lang(PackOutput output) {
			super(output, CulinaryCompat.MODID, "en_us");
		}

		@Override
		protected void addTranslations() {
		}
	}

	static final class Recipes extends RecipeProvider {
		Recipes(PackOutput packOutput) {
			super(packOutput);
		}

		@Override
		protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		}
	}
}
