package com.hoshihoku.culinarycompat.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
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
				BakeStatusPacket::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static void sendToClient(ServerPlayer player, BakeStatusPacket packet) {
		CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
