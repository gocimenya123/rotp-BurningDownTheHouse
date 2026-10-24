package com.rotpaddon.bdth.network.packets;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.rotpaddon.bdth.ghost.GhostComputerService;
import com.rotpaddon.bdth.ghost.GhostUtil;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/** Client to server: a question typed into the ghost computer. */
public class GhostComputerQueryPacket {
    public static final int MAX_LENGTH = 64;
    private final String query;

    public GhostComputerQueryPacket(String query) {
        this.query = query.length() > MAX_LENGTH ? query.substring(0, MAX_LENGTH) : query;
    }

    public static class Handler implements IModPacketHandler<GhostComputerQueryPacket> {

        @Override
        public void encode(GhostComputerQueryPacket msg, PacketBuffer buf) {
            buf.writeUtf(msg.query, MAX_LENGTH);
        }

        @Override
        public GhostComputerQueryPacket decode(PacketBuffer buf) {
            return new GhostComputerQueryPacket(buf.readUtf(MAX_LENGTH));
        }

        @Override
        public void handle(GhostComputerQueryPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null && GhostUtil.canTouchGhosts(player)) {
                GhostComputerService.answer(player, msg.query);
            }
        }

        @Override
        public Class<GhostComputerQueryPacket> getPacketClass() {
            return GhostComputerQueryPacket.class;
        }
    }
}
