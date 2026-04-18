package com.hoshihoku.culinarycompat.bridges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class BridgeRegistry {
	public record Entry(String source, ResourceLocation bridgeId, List<Ingredient> inputs, ItemStack output,
			ItemStack workstation, float experience) {
		public Entry(String source, ResourceLocation bridgeId, List<Ingredient> inputs, ItemStack output,
				ItemStack workstation) {
			this(source, bridgeId, inputs, output, workstation, 0f);
		}
	}

	private static final Map<ResourceLocation, Entry> ENTRIES = new ConcurrentHashMap<>();

	private BridgeRegistry() {
	}

	public static void clearBySource(String source) {
		ENTRIES.values().removeIf(e -> e.source().equals(source));
	}

	public static void register(Entry entry) {
		ENTRIES.put(entry.bridgeId(), entry);
	}

	public static Collection<Entry> getAll() {
		return Collections.unmodifiableCollection(new ArrayList<>(ENTRIES.values()));
	}

	public static int size() {
		return ENTRIES.size();
	}

	public static float findExperienceFor(String source, ResourceLocation outputId) {
		for (Entry e : ENTRIES.values()) {
			if (!e.source().equals(source))
				continue;
			ItemStack out = e.output();
			if (out.isEmpty())
				continue;
			ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(out.getItem());
			if (outputId.equals(id))
				return e.experience();
		}
		return 0f;
	}
}
