package io.github.smokahs.culinarycompat.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import io.github.smokahs.culinarycompat.CulinaryCompat;

public final class Items {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			CulinaryCompat.MODID);

	public static final RegistryObject<Item> BAKEWARE = ITEMS.register("bakeware",
			() -> new BlockItem(Blocks.BAKEWARE.get(), new Item.Properties().stacksTo(1)));

	private Items() {
	}
}
