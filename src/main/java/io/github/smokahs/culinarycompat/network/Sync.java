package io.github.smokahs.culinarycompat.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import io.github.smokahs.culinarycompat.bridges.Bridges;

//sync bridge entries so EMI/JEI can show them on dedicated servers
public record Sync(List<Bridges.Entry> entries) {

	public static void encode(Sync msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.entries.size());
		for (Bridges.Entry e : msg.entries) {
			buf.writeUtf(e.source());
			buf.writeResourceLocation(e.bridgeId());
			buf.writeVarInt(e.inputs().size());
			for (Ingredient ing : e.inputs()) {
				ing.toNetwork(buf);
			}
			buf.writeItem(e.output());
			buf.writeItem(e.workstation());
			buf.writeFloat(e.experience());
		}
	}

	public static Sync decode(FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<Bridges.Entry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			String source = buf.readUtf();
			ResourceLocation bridgeId = buf.readResourceLocation();
			int inputCount = buf.readVarInt();
			List<Ingredient> inputs = new ArrayList<>(inputCount);
			for (int j = 0; j < inputCount; j++) {
				inputs.add(Ingredient.fromNetwork(buf));
			}
			ItemStack output = buf.readItem();
			ItemStack workstation = buf.readItem();
			float experience = buf.readFloat();
			entries.add(new Bridges.Entry(source, bridgeId, inputs, output, workstation, experience));
		}
		return new Sync(entries);
	}

	public static void handle(Sync msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyOnClient(msg.entries)));
		ctx.get().setPacketHandled(true);
	}

	// integrated server shares the static map with its client, so only remote
	// clients need the sync
	private static void applyOnClient(List<Bridges.Entry> entries) {
		if (Minecraft.getInstance().hasSingleplayerServer()) {
			return;
		}
		Bridges.replaceAll(entries);
		if (ModList.get().isLoaded("emi")) {
			io.github.smokahs.culinarycompat.compat.emi.Plugin.requestReload();
		} else if (ModList.get().isLoaded("jei")) {
			io.github.smokahs.culinarycompat.compat.jei.Plugin.applyRuntimeSync();
		}
	}
}
