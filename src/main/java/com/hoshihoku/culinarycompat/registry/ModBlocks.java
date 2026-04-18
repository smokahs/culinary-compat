package com.hoshihoku.culinarycompat.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.hoshihoku.culinarycompat.CulinaryCompat;
import com.hoshihoku.culinarycompat.content.BakewareBlock;

public final class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			CulinaryCompat.MODID);

	public static final RegistryObject<Block> BAKEWARE = BLOCKS.register("bakeware",
			() -> new BakewareBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f)
					.sound(SoundType.METAL).noOcclusion()));

	private ModBlocks() {
	}
}
