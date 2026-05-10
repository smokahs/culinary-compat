package io.github.smokahs.culinarycompat.compat.ae2;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import io.github.smokahs.culinarycompat.CulinaryCompat;

// i should probably make balm required
public final class Bridge {
	public static final String AE2 = "ae2";
	public static final String CFB = "cookingforblockheads";
	public static final String BALM = "balm";

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			CulinaryCompat.MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			CulinaryCompat.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CulinaryCompat.MODID);
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
			CulinaryCompat.MODID);

	public static RegistryObject<Block> KITCHEN_STATION;
	public static RegistryObject<Item> KITCHEN_STATION_ITEM;
	public static RegistryObject<BlockEntityType<StationBlockEntity>> KITCHEN_STATION_BE;
	public static RegistryObject<MenuType<StationMenu>> KITCHEN_STATION_MENU;

	private static boolean active = false;

	private Bridge() {
	}

	public static boolean isActive() {
		return active;
	}

	public static void init(IEventBus modEventBus) {
		if (!ModList.get().isLoaded(AE2) || !ModList.get().isLoaded(CFB) || !ModList.get().isLoaded(BALM)) {
			return;
		}
		active = true;

		KITCHEN_STATION = BLOCKS.register("ae2_kitchen_station", StationBlock::new);
		KITCHEN_STATION_ITEM = ITEMS.register("ae2_kitchen_station",
				() -> new StationItem((StationBlock) KITCHEN_STATION.get(), new Item.Properties()));
		KITCHEN_STATION_BE = BLOCK_ENTITIES.register("ae2_kitchen_station",
				() -> BlockEntityType.Builder.of(StationBlockEntity::new, KITCHEN_STATION.get()).build(null));
		KITCHEN_STATION_MENU = MENUS.register("ae2_kitchen_station", () -> IForgeMenuType.create(StationMenu::new));

		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		BLOCK_ENTITIES.register(modEventBus);
		MENUS.register(modEventBus);
		modEventBus.addListener(Bridge::onCommonSetup);

		CulinaryCompat.LOGGER.info("AE2 bridge active: ae2_kitchen_station registered");
	}

	private static void onCommonSetup(FMLCommonSetupEvent e) {
		appeng.api.features.GridLinkables.register(KITCHEN_STATION_ITEM.get(), StationItem.LINKABLE_HANDLER);
	}
}
