package com.hoshihoku.culinarycompat.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.content.BakewareBlock;

public final class ModRegistry {
	private ModRegistry() {
	}

	public static final class Blocks {
		public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
				CulinaryCompat.MODID);

		public static final RegistryObject<Block> BAKEWARE = BLOCKS.register("bakeware",
				() -> new BakewareBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f)
						.sound(SoundType.METAL).noOcclusion()));

		private Blocks() {
		}
	}

	public static final class Items {
		public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
				CulinaryCompat.MODID);

		public static final RegistryObject<Item> BAKEWARE = ITEMS.register("bakeware",
				() -> new BlockItem(Blocks.BAKEWARE.get(), new Item.Properties().stacksTo(1)));

		private Items() {
		}
	}

	public static final class Sounds {
		public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
				CulinaryCompat.MODID);

		public static final RegistryObject<SoundEvent> DING = SOUNDS.register("ding",
				() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CulinaryCompat.MODID, "ding")));

		private Sounds() {
		}
	}
}
