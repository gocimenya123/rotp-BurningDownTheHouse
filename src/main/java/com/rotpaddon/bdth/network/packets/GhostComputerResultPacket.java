package com.rotpaddon.bdth.network.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.rotpaddon.bdth.client.ClientBridge;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

/** Server to client: the lines the ghost computer prints in answer to a query. */
public class GhostComputerResultPacket {
    public final List<ITextComponent> lines;

    public GhostComputerResultPacket(List<ITextComponent> lines) {
        this.lines = lines;
    }

    public static class Handler implements IModPacketHandler<GhostComputerResultPacket> {

        @Override
        public void encode(GhostComputerResultPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.lines.size());
            msg.lines.forEach(buf::writeComponent);
        }

        @Override
        public GhostComputerResultPacket decode(PacketBuffer buf) {
            int size = buf.readVarInt();
            List<ITextComponent> lines = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                lines.add(buf.readComponent());
            }
            return new GhostComputerResultPacket(lines);
        }

        @Override
        public void handle(GhostComputerResultPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientBridge.onComputerResult(msg.lines);
        }

        @Override
        public Class<GhostComputerResultPacket> getPacketClass() {
            return GhostComputerResultPacket.class;
        }
    }
}
