package com.hoshihoku.culinarycompat.bridges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

public final class Bridges {
	private Bridges() {
	}

	public enum Kind {
		CUTTINGBOARD("cuttingboard", Set.of("pam_cuttingboard", "fd_cutting")), SKILLET("skillet",
				Set.of("pam_skillet")), OVEN("oven", Set.of("campfire_oven")), POT("pot",
						Set.of("pam_pot", "fd_cooking")), BAKEWARE("bakeware", Set.of("pam_bakeware"));

		public final String path;
		public final Set<String> sources;

		Kind(String path, Set<String> sources) {
			this.path = path;
			this.sources = sources;
		}

		public static Kind fromSource(String source) {
			for (Kind k : values()) {
				if (k.sources.contains(source))
					return k;
			}
			return null;
		}
	}

	public record Entry(String source, ResourceLocation bridgeId, List<Ingredient> inputs, ItemStack output,
			ItemStack workstation, float experience) {
		public Entry(String source, ResourceLocation bridgeId, List<Ingredient> inputs, ItemStack output,
				ItemStack workstation) {
			this(source, bridgeId, inputs, output, workstation, 0f);
		}
	}

	private static final Map<ResourceLocation, Entry> ENTRIES = new ConcurrentHashMap<>();

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
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(out.getItem());
			if (outputId.equals(id))
				return e.experience();
		}
		return 0f;
	}
}
