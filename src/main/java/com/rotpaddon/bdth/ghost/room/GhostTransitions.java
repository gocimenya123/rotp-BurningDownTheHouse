package com.rotpaddon.bdth.ghost.room;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.rotpaddon.bdth.network.BdthNetwork;
import com.rotpaddon.bdth.network.packets.GhostSquashPacket;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The few ticks between stepping into a crack and appearing on the other side, during which everyone who sees
 * the player watches the body get squeezed flat against the wall (see {@link GhostSquashPacket}).
 */
public final class GhostTransitions {
    public static final int SQUASH_TICKS = 16;
    public static final int EXPAND_TICKS = 12;

    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private GhostTransitions() {}

    public static boolean isInTransition(ServerPlayerEntity player) {
        return PENDING.containsKey(player.getUUID());
    }

    /** @param onFinish runs on the server once the body is flat; it is expected to teleport the player */
    public static void start(ServerPlayerEntity player, Direction normal, Runnable onFinish) {
        if (isInTransition(player)) {
            return;
        }
        PENDING.put(player.getUUID(), new Pending(player, player.server.getTickCount() + SQUASH_TICKS, onFinish));
        sendSquash(player, normal, SQUASH_TICKS, true);
    }

    public static void sendSquash(ServerPlayerEntity player, Direction normal, int duration, boolean squashing) {
        BdthNetwork.sendToClientsTrackingAndSelf(
                new GhostSquashPacket(player.getId(), normal.getStepX(), normal.getStepZ(), duration, squashing), player);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        Iterator<Pending> it = PENDING.values().iterator();
        while (it.hasNext()) {
            Pending pending = it.next();
            if (!pending.player.isAlive() || pending.player.server.getPlayerList().getPlayer(pending.player.getUUID()) != pending.player) {
                it.remove();
            }
            else if (pending.player.server.getTickCount() >= pending.finishTick) {
                it.remove();
                pending.onFinish.run();
            }
        }
    }

    private static class Pending {
        final ServerPlayerEntity player;
        final int finishTick;
        final Runnable onFinish;

        Pending(ServerPlayerEntity player, int finishTick, Runnable onFinish) {
            this.player = player;
            this.finishTick = finishTick;
            this.onFinish = onFinish;
        }
    }
}
