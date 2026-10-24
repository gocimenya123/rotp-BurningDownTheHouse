package com.rotpaddon.bdth.ghost.room;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.capability.GhostCrackLocation;
import com.rotpaddon.bdth.capability.NbtCapabilityStorage;
import com.rotpaddon.bdth.init.InitDimensions;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * The ghost room dimension's data (a capability of that world, saved with it, the way Mr. President's rooms are):
 * which cell of the dimension belongs to which user, whether it has been built, and where leaving it leads.
 * A room is identified by its owner's UUID.
 */
public class GhostRoomWorldData implements INBTSerializable<CompoundNBT> {
    @CapabilityInject(GhostRoomWorldData.class)
    public static Capability<GhostRoomWorldData> CAPABILITY = null;

    /** distance between the rooms' corners, so that neighbouring rooms never touch */
    public static final int CELL_SIZE = 32;
    public static final int ROOM_Y = 64;

    private final Map<UUID, Room> rooms = new HashMap<>();
    private int nextRoomIndex = 0;

    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(GhostRoomWorldData.class, new NbtCapabilityStorage<>(), GhostRoomWorldData::new);
    }

    public static LazyOptional<GhostRoomWorldData> get(@Nullable ServerWorld ghostWorld) {
        return ghostWorld != null ? ghostWorld.getCapability(CAPABILITY) : LazyOptional.empty();
    }

    public static LazyOptional<GhostRoomWorldData> get(MinecraftServer server) {
        return get(server.getLevel(InitDimensions.GHOST_ROOM));
    }

    public Room getOrCreateRoom(UUID owner) {
        Room room = rooms.get(owner);
        if (room == null) {
            room = new Room(nextRoomIndex++);
            rooms.put(owner, room);
        }
        return room;
    }

    @Nullable
    public Room getRoom(UUID owner) {
        return rooms.get(owner);
    }

    /** the room this player last entered, wherever they are standing (used when the shell was left) */
    @Nullable
    public Room getRoomWithEntry(UUID player) {
        for (Room room : rooms.values()) {
            if (room.getEntry(player) != null) {
                return room;
            }
        }
        return null;
    }

    /** the owner of the room that this position is inside of */
    @Nullable
    public UUID getRoomAt(BlockPos pos) {
        for (Map.Entry<UUID, Room> entry : rooms.entrySet()) {
            if (entry.getValue().contains(pos)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        ListNBT list = new ListNBT();
        rooms.forEach((owner, room) -> {
            CompoundNBT nbt = room.toNBT();
            nbt.putUUID("Owner", owner);
            list.add(nbt);
        });
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Rooms", list);
        nbt.putInt("NextIndex", nextRoomIndex);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        rooms.clear();
        nextRoomIndex = nbt.getInt("NextIndex");
        for (INBT tag : nbt.getList("Rooms", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT roomNbt = (CompoundNBT) tag;
            if (roomNbt.hasUUID("Owner")) {
                Room room = Room.fromNBT(roomNbt);
                rooms.put(roomNbt.getUUID("Owner"), room);
                nextRoomIndex = Math.max(nextRoomIndex, room.index + 1);
            }
        }
    }

    public static class Room {
        private final int index;
        private boolean built;
        private long lastRestock;
        /** fallback exit (the owner's last entry), kept for worlds saved before per-player entries */
        @Nullable
        private GhostCrackLocation exit;
        /** where each player entered this room from; leaving the room uses the player's own crack */
        private final Map<UUID, GhostCrackLocation> entries = new HashMap<>();
        /**
         * Who the owner has invited. The invitation list lives on the owner's player data, which cannot be read
         * while they are away, so the room keeps its own copy: the crack stays usable for the guests whoever is
         * online and wherever the owner is.
         */
        private final Set<UUID> guests = new LinkedHashSet<>();

        private Room(int index) {
            this.index = index;
        }

        /** the lowest corner of the room's shell */
        public BlockPos getOrigin() {
            int[] cell = spiralCell(index);
            return new BlockPos(cell[0] * CELL_SIZE, ROOM_Y, cell[1] * CELL_SIZE);
        }

        public boolean contains(BlockPos pos) {
            BlockPos o = getOrigin();
            return pos.getX() >= o.getX() && pos.getX() < o.getX() + GhostRoomBuilder.SIZE_XZ
                    && pos.getZ() >= o.getZ() && pos.getZ() < o.getZ() + GhostRoomBuilder.SIZE_XZ
                    && pos.getY() >= o.getY() && pos.getY() < o.getY() + GhostRoomBuilder.SIZE_Y;
        }

        public boolean isBuilt() {
            return built;
        }

        public void setBuilt() {
            this.built = true;
        }

        public long getLastRestock() {
            return lastRestock;
        }

        public void setLastRestock(long gameTime) {
            this.lastRestock = gameTime;
        }

        /** where leaving the room leads: the crack that was last used to come in */
        @Nullable
        public GhostCrackLocation getExit() {
            return exit;
        }

        public void setExit(GhostCrackLocation exit) {
            this.exit = exit;
        }

        /** the crack this player used to get into the room (the best place to send them back to) */
        @Nullable
        public GhostCrackLocation getEntry(UUID player) {
            return entries.get(player);
        }

        public void setEntry(UUID player, GhostCrackLocation location) {
            entries.put(player, location);
        }

        /** whether the owner has invited this player into the room */
        public boolean isGuest(UUID player) {
            return guests.contains(player);
        }

        /** replaces the copy of the invitation list with the owner's current one */
        public void setGuests(Collection<UUID> invited) {
            if (guests.size() == invited.size() && guests.containsAll(invited)) {
                return;
            }
            guests.clear();
            guests.addAll(invited);
        }

        private CompoundNBT toNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Index", index);
            nbt.putBoolean("Built", built);
            nbt.putLong("LastRestock", lastRestock);
            if (exit != null) {
                nbt.put("Exit", exit.toNBT());
            }
            ListNBT entryList = new ListNBT();
            entries.forEach((player, location) -> {
                CompoundNBT entryNbt = new CompoundNBT();
                entryNbt.putUUID("Player", player);
                entryNbt.put("Exit", location.toNBT());
                entryList.add(entryNbt);
            });
            nbt.put("Entries", entryList);
            ListNBT guestList = new ListNBT();
            for (UUID guest : guests) {
                guestList.add(NBTUtil.createUUID(guest));
            }
            nbt.put("Guests", guestList);
            return nbt;
        }

        private static Room fromNBT(CompoundNBT nbt) {
            Room room = new Room(nbt.getInt("Index"));
            room.built = nbt.getBoolean("Built");
            room.lastRestock = nbt.getLong("LastRestock");
            if (nbt.contains("Exit", Constants.NBT.TAG_COMPOUND)) {
                room.exit = GhostCrackLocation.fromNBT(nbt.getCompound("Exit"));
            }
            for (INBT tag : nbt.getList("Entries", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT entryNbt = (CompoundNBT) tag;
                if (entryNbt.hasUUID("Player") && entryNbt.contains("Exit", Constants.NBT.TAG_COMPOUND)) {
                    GhostCrackLocation location = GhostCrackLocation.fromNBT(entryNbt.getCompound("Exit"));
                    if (location != null) {
                        room.entries.put(entryNbt.getUUID("Player"), location);
                    }
                }
            }
            for (INBT tag : nbt.getList("Guests", Constants.NBT.TAG_INT_ARRAY)) {
                room.guests.add(NBTUtil.loadUUID(tag));
            }
            return room;
        }

        /** the n-th cell on a square spiral around (0, 0): (0,0), (1,0), (1,1), (0,1), (-1,1), ... */
        static int[] spiralCell(int n) {
            int x = 0, z = 0, dx = 1, dz = 0;
            int segmentLength = 1, segmentPassed = 0, turns = 0;
            for (int i = 0; i < n; i++) {
                x += dx;
                z += dz;
                if (++segmentPassed == segmentLength) {
                    segmentPassed = 0;
                    int tmp = dx;
                    dx = -dz;
                    dz = tmp;
                    if (++turns % 2 == 0) {
                        segmentLength++;
                    }
                }
            }
            return new int[] {x, z};
        }
    }
}
