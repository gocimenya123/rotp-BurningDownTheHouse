package com.rotpaddon.bdth.network;

import java.util.Optional;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.network.packets.GhostComputerQueryPacket;
import com.rotpaddon.bdth.network.packets.GhostComputerResultPacket;
import com.rotpaddon.bdth.network.packets.GhostDataSyncPacket;
import com.rotpaddon.bdth.network.packets.GhostSquashPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/** The addon's network channel. Packet classes use the same {@link IModPacketHandler} pattern as Ripples of the Past. */
public class BdthNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel channel;
    private static int packetIndex = 0;

    public static void init() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BdthAddon.MOD_ID, "main"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        packetIndex = 0;
        register(new GhostDataSyncPacket.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        register(new GhostSquashPacket.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        register(new GhostComputerResultPacket.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        register(new GhostComputerQueryPacket.Handler(), NetworkDirection.PLAY_TO_SERVER);
    }

    private static <MSG> void register(IModPacketHandler<MSG> handler, NetworkDirection direction) {
        channel.registerMessage(packetIndex++, handler.getPacketClass(), handler::encode, handler::decode, 
                handler::enqueueHandleSetHandled, Optional.of(direction));
    }

    public static void sendToServer(Object msg) {
        channel.sendToServer(msg);
    }

    public static void sendToClient(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer)) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
    }

    public static void sendToClientsTrackingAndSelf(Object msg, Entity entity) {
        channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }
}
