package com.hoshihoku.culinarycompat.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.hoshihoku.culinarycompat.CulinaryCompat;

public final class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			CulinaryCompat.MODID);

	public static final RegistryObject<Item> BAKEWARE = ITEMS.register("bakeware",
			() -> new BlockItem(ModBlocks.BAKEWARE.get(), new Item.Properties().stacksTo(1)));

	private ModItems() {
	}
}
