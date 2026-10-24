package com.rotpaddon.bdth.network.packets;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.rotpaddon.bdth.client.ClientBridge;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/** Server to owner: the part of {@code GhostUserData} that the client needs (invitations, how full the pocket is). */
public class GhostDataSyncPacket {
    private final CompoundNBT data;

    public GhostDataSyncPacket(CompoundNBT data) {
        this.data = data;
    }

    public static class Handler implements IModPacketHandler<GhostDataSyncPacket> {

        @Override
        public void encode(GhostDataSyncPacket msg, PacketBuffer buf) {
            buf.writeNbt(msg.data);
        }

        @Override
        public GhostDataSyncPacket decode(PacketBuffer buf) {
            return new GhostDataSyncPacket(buf.readNbt());
        }

        @Override
        public void handle(GhostDataSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientBridge.onGhostDataSync(msg.data);
        }

        @Override
        public Class<GhostDataSyncPacket> getPacketClass() {
            return GhostDataSyncPacket.class;
        }
    }
}
