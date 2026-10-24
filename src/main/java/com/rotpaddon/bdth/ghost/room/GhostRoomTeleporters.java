package com.rotpaddon.bdth.ghost.room;

import java.util.UUID;
import java.util.function.Function;

import com.rotpaddon.bdth.capability.GhostCrackLocation;

import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

/**
 * The two {@link ITeleporter}s, used the same way Mr. President's room does it.
 * <p>
 * The destination is returned from {@link #getPortalInfo}, not applied with {@code moveTo} after the fact:
 * Forge sends the vanilla position packet before {@code placeEntity} runs, and the client's teleport confirm
 * would then snap the player back to the source coordinates (for a room that is the ghost dimension's ~0,0).
 */
public final class GhostRoomTeleporters {
    private GhostRoomTeleporters() {}

    /** into the ghost room of the given owner; the room is built on first entry */
    public static class Entry implements ITeleporter {
        private final UUID roomOwner;

        public Entry(UUID roomOwner) {
            this.roomOwner = roomOwner;
        }

        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld,
                Function<ServerWorld, PortalInfo> defaultPortalInfo) {
            GhostRoomWorldData data = GhostRoomWorldData.get(destWorld).orElse(null);
            if (data == null) {
                return new PortalInfo(entity.position(), Vector3d.ZERO, GhostRoomBuilder.getEntryYaw(), 0.0F);
            }
            BlockPos origin = data.getOrCreateRoom(roomOwner).getOrigin();
            return new PortalInfo(GhostRoomBuilder.getEntryPos(origin), Vector3d.ZERO,
                    GhostRoomBuilder.getEntryYaw(), 0.0F);
        }

        @Override
        public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld,
                float yaw, Function<Boolean, Entity> repositionEntity) {
            GhostRoomWorldData data = GhostRoomWorldData.get(destWorld).orElse(null);
            if (data != null) {
                GhostRoomWorldData.Room room = data.getOrCreateRoom(roomOwner);
                BlockPos origin = room.getOrigin();
                if (!room.isBuilt()) {
                    GhostRoomBuilder.build(destWorld, origin, roomOwner);
                    room.setBuilt();
                    room.setLastRestock(destWorld.getGameTime());
                }
                else if (destWorld.getGameTime() - room.getLastRestock() >= GhostRoomBuilder.RESTOCK_PERIOD) {
                    GhostRoomBuilder.restock(destWorld, origin);
                    room.setLastRestock(destWorld.getGameTime());
                }
            }
            // the vanilla logic reads the position from getPortalInfo above and sends it to the client
            return repositionEntity.apply(false);
        }

        @Override
        public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld) {
            return false;
        }
    }

    /** out of the room, to the point in front of a crack of the normal world */
    public static class Exit implements ITeleporter {
        private final Vector3d pos;
        private final float yaw;

        public Exit(GhostCrackLocation location) {
            this(location.exitPos(), location.exitYaw());
        }

        public Exit(Vector3d pos, float yaw) {
            this.pos = pos;
            this.yaw = yaw;
        }

        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld,
                Function<ServerWorld, PortalInfo> defaultPortalInfo) {
            return new PortalInfo(pos, Vector3d.ZERO, yaw, 0.0F);
        }

        @Override
        public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld,
                float oldYaw, Function<Boolean, Entity> repositionEntity) {
            // the vanilla logic reads the position from getPortalInfo above and sends it to the client
            return repositionEntity.apply(false);
        }

        @Override
        public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld) {
            return false;
        }
    }
}
