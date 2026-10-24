package com.rotpaddon.bdth.ghost.room;

import java.util.UUID;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.capability.GhostCrackLocation;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.entity.GhostCrackEntity;
import com.rotpaddon.bdth.ghost.GhostUtil;
import com.rotpaddon.bdth.init.InitDimensions;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The rules of the ghost room: who may go in, going in and out through cracks, and sending out whoever has no
 * business being in there. The only place that moves entities between the normal world and the room.
 */
public final class GhostRoomService {
    private static final int EXPEL_CHECK_PERIOD = 10;

    private GhostRoomService() {}

    // ---------------------------------------------------------------- rules

    /**
     * The owner goes in whenever they want. The invited may go in as soon as the owner has invited them: the owner
     * does not have to be in the room, or even online - the room keeps its own copy of the invitation list.
     */
    public static boolean canEnter(MinecraftServer server, UUID roomOwner, ServerPlayerEntity who) {
        GhostRoomWorldData data = GhostRoomWorldData.get(server).orElse(null);
        GhostRoomWorldData.Room room = data != null ? data.getRoom(roomOwner) : null;
        ServerPlayerEntity owner = server.getPlayerList().getPlayer(roomOwner);
        if (owner == null) {
            // the owner is away: the invitation list the room was left with is all there is to go by
            return room != null && room.isGuest(who.getUUID());
        }
        if (!GhostUtil.isBurningDownTheHouseUser(owner)) {
            return false;
        }
        GhostUserData ownerData = GhostUserData.get(owner).orElse(null);
        if (ownerData == null) {
            return false;
        }
        if (room != null) {
            // keep the room's copy in step while the list can be read
            room.setGuests(ownerData.getInvited());
        }
        return who.getUUID().equals(roomOwner) || ownerData.isInvited(who.getUUID());
    }

    /** Copies the owner's invitation list into their room, so that guests can get in while the owner is away. */
    public static void syncInvitations(ServerPlayerEntity owner) {
        GhostRoomWorldData data = GhostRoomWorldData.get(owner.server).orElse(null);
        GhostRoomWorldData.Room room = data != null ? data.getRoom(owner.getUUID()) : null;
        if (room == null) {
            return;
        }
        GhostUserData.get(owner).ifPresent(userData -> room.setGuests(userData.getInvited()));
    }

    /** the nearest crack that belongs to the given owner (the player's own crack) */
    @Nullable
    public static GhostCrackEntity findCrackNear(PlayerEntity player, UUID owner, double radius) {
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        GhostCrackEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (GhostCrackEntity crack : player.level.getEntitiesOfClass(GhostCrackEntity.class, area, 
                crack -> !crack.isRoomExit() && owner.equals(crack.getOwnerId()))) {
            double dist = crack.distanceToSqr(player);
            if (dist < best) {
                best = dist;
                nearest = crack;
            }
        }
        return nearest;
    }

    /** the nearest crack of any owner; used on the client, where the invitation check cannot run */
    @Nullable
    public static GhostCrackEntity findAnyCrackNear(PlayerEntity player, double radius) {
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        GhostCrackEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (GhostCrackEntity crack : player.level.getEntitiesOfClass(GhostCrackEntity.class, area,
                crack -> !crack.isRoomExit() && crack.getOwnerId() != null)) {
            double dist = crack.distanceToSqr(player);
            if (dist < best) {
                best = dist;
                nearest = crack;
            }
        }
        return nearest;
    }

    /** the nearest crack of another owner that this player is allowed to use (an invitation) */
    @Nullable
    public static GhostCrackEntity findEnterableCrack(ServerPlayerEntity player, double radius) {
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        GhostCrackEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (GhostCrackEntity crack : player.level.getEntitiesOfClass(GhostCrackEntity.class, area, 
                crack -> !crack.isRoomExit())) {
            UUID owner = crack.getOwnerId();
            if (owner != null && canEnter(player.server, owner, player)) {
                double dist = crack.distanceToSqr(player);
                if (dist < best) {
                    best = dist;
                    nearest = crack;
                }
            }
        }
        return nearest;
    }

    // ---------------------------------------------------------------- passages

    /** a right click on a crack in the world, or on the exit crack inside the room */
    public static void useCrack(GhostCrackEntity crack, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity user = (ServerPlayerEntity) player;
        if (crack.isRoomExit()) {
            if (GhostUtil.isInGhostRoom(user)) {
                leave(user, crack.getNormal());
            }
            return;
        }
        UUID owner = crack.getOwnerId();
        if (owner == null || !canEnter(user.server, owner, user)) {
            user.displayClientMessage(new TranslationTextComponent("message.burningdownthehouse.crack_unknown"), true);
            return;
        }
        enter(user, owner, crack);
    }

    public static void enter(ServerPlayerEntity player, UUID roomOwner, GhostCrackEntity crack) {
        MinecraftServer server = player.server;
        ServerWorld ghostWorld = server.getLevel(InitDimensions.GHOST_ROOM);
        GhostRoomWorldData data = GhostRoomWorldData.get(ghostWorld).orElse(null);
        if (data == null) {
            player.displayClientMessage(new TranslationTextComponent("message.burningdownthehouse.no_dimension"), true);
            return;
        }
        GhostRoomWorldData.Room room = data.getOrCreateRoom(roomOwner);
        GhostCrackLocation entry = new GhostCrackLocation(player.level.dimension(), 
                new Vector3d(crack.getX(), crack.getY(), crack.getZ()), crack.getNormal());
        // every player leaves through the same crack they came in by, whoever owns the room
        room.setEntry(player.getUUID(), entry);
        if (player.getUUID().equals(roomOwner)) {
            room.setExit(entry);
        }
        GhostTransitions.start(player, crack.getNormal().getOpposite(), () -> {
            player.changeDimension(ghostWorld, new GhostRoomTeleporters.Entry(roomOwner));
            GhostTransitions.sendSquash(player, Direction.SOUTH, GhostTransitions.EXPAND_TICKS, false);
        });
    }

    /** leaves the room the player is in, through its exit crack (with the squeeze) */
    public static void leave(ServerPlayerEntity player, Direction exitCrackNormal) {
        if (!GhostUtil.isInGhostRoom(player)) {
            return;
        }
        GhostTransitions.start(player, exitCrackNormal.getOpposite(), () -> teleportOut(player, true));
    }

    /** the room the player is in, or else the one they own / last entered (for when they left the shell) */
    @Nullable
    private static GhostRoomWorldData.Room findRoomFor(@Nullable GhostRoomWorldData data, ServerPlayerEntity player) {
        if (data == null) {
            return null;
        }
        UUID at = data.getRoomAt(player.blockPosition());
        if (at != null) {
            return data.getRoom(at);
        }
        GhostRoomWorldData.Room own = data.getRoom(player.getUUID());
        return own != null ? own : data.getRoomWithEntry(player.getUUID());
    }

    /** Takes the player out of the room right away, without the squeeze (the owner is gone, the exit is lost, ...). */
    public static void teleportOut(ServerPlayerEntity player, boolean withExpandEffect) {
        MinecraftServer server = player.server;
        GhostRoomWorldData data = GhostRoomWorldData.get(server).orElse(null);
        GhostRoomWorldData.Room room = findRoomFor(data, player);
        GhostCrackLocation exit = room != null ? room.getEntry(player.getUUID()) : null;
        if (exit == null && room != null) {
            exit = room.getExit();
        }

        ServerWorld destination = exit != null ? server.getLevel(exit.dimension) : null;
        Direction facing = Direction.SOUTH;
        if (destination != null) {
            player.changeDimension(destination, new GhostRoomTeleporters.Exit(exit));
            facing = exit.normal;
        }
        else {
            ServerWorld overworld = server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            player.changeDimension(overworld, new GhostRoomTeleporters.Exit(
                    new Vector3d(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5), 0));
        }
        if (withExpandEffect) {
            GhostTransitions.sendSquash(player, facing, GhostTransitions.EXPAND_TICKS, false);
        }
    }

    // ---------------------------------------------------------------- upkeep

    /** Puts the owner back at their room's entrance without leaving the ghost dimension. */
    private static void returnToOwnRoom(ServerPlayerEntity player, GhostRoomWorldData.Room room) {
        ServerWorld world = player.server.getLevel(InitDimensions.GHOST_ROOM);
        if (world == null) {
            return;
        }
        BlockPos origin = room.getOrigin();
        if (!room.isBuilt()) {
            GhostRoomBuilder.build(world, origin, player.getUUID());
            room.setBuilt();
            room.setLastRestock(world.getGameTime());
        }
        Vector3d pos = GhostRoomBuilder.getEntryPos(origin);
        player.teleportTo(pos.x, pos.y, pos.z);
        player.setDeltaMovement(Vector3d.ZERO);
        player.fallDistance = 0.0F;
    }

    /**
     * A guest who came into a room through its crack is invited by definition and stays there even after the owner
     * has left (or logged off): the room is not closed behind the owner, only strangers have no business in it.
     * The owner of a room in this dimension is never sent out, whatever position they are at - the owner can fly
     * around in creative, be just outside the shell, or the shell may be broken, and still stay inside.
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof ServerWorld)
                || event.world.dimension() != InitDimensions.GHOST_ROOM
                || event.world.getGameTime() % EXPEL_CHECK_PERIOD != 0) {
            return;
        }
        ServerWorld world = (ServerWorld) event.world;
        GhostRoomWorldData data = GhostRoomWorldData.get(world).orElse(null);
        if (data == null) {
            return;
        }
        for (ServerPlayerEntity player : world.players().toArray(new ServerPlayerEntity[0])) {
            if (GhostTransitions.isInTransition(player)) {
                continue;
            }
            UUID roomOwner = data.getRoomAt(player.blockPosition());
            if (roomOwner == null) {
                // Outside every room shell. Do not kick out anyone who owns a room in this dimension
                // (a creative flight or a step through the shell must not throw the owner back to the world).
                GhostRoomWorldData.Room ownRoom = data.getRoom(player.getUUID());
                if (ownRoom == null) {
                    teleportOut(player, true);
                }
                else if (player.getY() < ownRoom.getOrigin().getY() - 8) {
                    // Fell out of the room (or appeared outside it): put the owner back inside, do not send them out.
                    returnToOwnRoom(player, ownRoom);
                }
                continue;
            }
            if (player.getUUID().equals(roomOwner)) {
                continue;
            }
            if (isGuestOf(world.getServer(), data, roomOwner, player)) {
                continue;
            }
            // A stranger in the room: sent out, the owner being there or not.
            teleportOut(player, true);
        }
    }

    /**
     * A player who came into this room through a crack has an entry on record and is its guest; so is anyone the
     * owner has invited. Guests keep the room after the owner leaves, whether the owner is inside or not.
     */
    private static boolean isGuestOf(MinecraftServer server, GhostRoomWorldData data, UUID roomOwner, ServerPlayerEntity player) {
        GhostRoomWorldData.Room room = data.getRoom(roomOwner);
        if (room != null && (room.getEntry(player.getUUID()) != null || room.isGuest(player.getUUID()))) {
            return true;
        }
        ServerPlayerEntity owner = server.getPlayerList().getPlayer(roomOwner);
        return owner != null && GhostUserData.get(owner).map(ownerData -> ownerData.isInvited(player.getUUID())).orElse(false);
    }
}
