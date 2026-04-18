package com.hoshihoku.culinarycompat.network;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import com.hoshihoku.culinarycompat.CulinaryCompat;

public final class NetworkHandler {
	private static final String VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(CulinaryCompat.MODID, "main")).networkProtocolVersion(() -> VERSION)
			.clientAcceptedVersions(VERSION::equals).serverAcceptedVersions(VERSION::equals).simpleChannel();

	private NetworkHandler() {
	}

	public static void register() {
		int id = 0;
		CHANNEL.registerMessage(id++, BakeStatusPacket.class, BakeStatusPacket::encode, BakeStatusPacket::decode,
				BakeStatusPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static void sendToClient(ServerPlayer player, BakeStatusPacket packet) {
		CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	public enum BakePhase {
		CONFIRM, BAKING, FINISHED, CANCELLED
	}

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
							() -> () -> com.hoshihoku.culinarycompat.compat.cfb.bake.BakeState.Client
									.onStatus(msg.phase, msg.outputId)));
			ctx.get().setPacketHandled(true);
		}
	}
}
