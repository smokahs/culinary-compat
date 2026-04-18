package com.hoshihoku.culinarycompat.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record BakeStatusPacket(BakePhase phase, ResourceLocation outputId) {
	public static void encode(BakeStatusPacket msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.phase);
		buf.writeResourceLocation(msg.outputId);
	}

	public static BakeStatusPacket decode(FriendlyByteBuf buf) {
		return new BakeStatusPacket(buf.readEnum(BakePhase.class), buf.readResourceLocation());
	}

	public static void handle(BakeStatusPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get()
				.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
						() -> () -> com.hoshihoku.culinarycompat.compat.cfb.bake.ClientBakeState.onStatus(msg.phase,
								msg.outputId)));
		ctx.get().setPacketHandled(true);
	}
}
