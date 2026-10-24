package com.rotpaddon.bdth.network.packets;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.rotpaddon.bdth.client.ClientBridge;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server to everyone who sees the entity: it is being squeezed through a crack (or is coming out of one).
 * Entering a crack flattens the body against the wall; leaving one restores it.
 */
public class GhostSquashPacket {
    public final int entityId;
    /** the horizontal normal of the crack's wall */
    public final int normalX;
    public final int normalZ;
    public final int duration;
    /** true: the body gets flat; false: it gets back to normal */
    public final boolean squashing;

    public GhostSquashPacket(int entityId, int normalX, int normalZ, int duration, boolean squashing) {
        this.entityId = entityId;
        this.normalX = normalX;
        this.normalZ = normalZ;
        this.duration = duration;
        this.squashing = squashing;
    }

    public static class Handler implements IModPacketHandler<GhostSquashPacket> {

        @Override
        public void encode(GhostSquashPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.entityId);
            buf.writeByte(msg.normalX);
            buf.writeByte(msg.normalZ);
            buf.writeVarInt(msg.duration);
            buf.writeBoolean(msg.squashing);
        }

        @Override
        public GhostSquashPacket decode(PacketBuffer buf) {
            return new GhostSquashPacket(buf.readVarInt(), buf.readByte(), buf.readByte(), buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void handle(GhostSquashPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientBridge.onSquash(msg);
        }

        @Override
        public Class<GhostSquashPacket> getPacketClass() {
            return GhostSquashPacket.class;
        }
    }
}
