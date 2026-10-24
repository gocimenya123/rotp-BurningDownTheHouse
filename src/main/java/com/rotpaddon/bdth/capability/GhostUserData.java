package com.rotpaddon.bdth.capability;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Per-player data of Burning Down the House: the pocket, the people invited into the room and the last crack.
 * <p>
 * The room itself is identified by the owner's UUID, so nothing about the room has to be stored here.
 * Attached to every player (the data is inert unless the player has the Stand), saved with the player,
 * copied on respawn and synced to the owning client by {@code GhostDataSyncPacket}
 * (the client only needs what action conditions are checked against: invitations and the pocket's fill).
 */
public class GhostUserData implements INBTSerializable<CompoundNBT> {
    @CapabilityInject(GhostUserData.class)
    public static Capability<GhostUserData> CAPABILITY = null;

    private final Set<UUID> invited = new LinkedHashSet<>();
    private final GhostPocket pocket = new GhostPocket();
    @Nullable
    private GhostCrackLocation crack;

    private boolean syncNeeded;
    // client-side mirror of the pocket (the pocket contents themselves are only synced while its container is open)
    private int clientPocketStacks;

    public GhostUserData() {
        pocket.addListener(inv -> syncNeeded = true);
    }

    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(GhostUserData.class, new NbtCapabilityStorage<>(), GhostUserData::new);
    }

    public static LazyOptional<GhostUserData> get(PlayerEntity player) {
        return player.getCapability(CAPABILITY);
    }

    // ---- invitations
    public Set<UUID> getInvited() {
        return Collections.unmodifiableSet(invited);
    }

    public boolean isInvited(UUID entityId) {
        return invited.contains(entityId);
    }

    /** @return true if the entity is invited after the call */
    public boolean toggleInvited(UUID entityId) {
        if (!invited.remove(entityId)) {
            invited.add(entityId);
            syncNeeded = true;
            return true;
        }
        syncNeeded = true;
        return false;
    }

    // ---- pocket
    public GhostPocket getPocket() {
        return pocket;
    }

    /** the number of occupied pocket slots; on the client this is the synced value */
    public int getPocketStacks(boolean clientSide) {
        return clientSide ? clientPocketStacks : pocket.countStacks();
    }

    // ---- crack
    @Nullable
    public GhostCrackLocation getCrack() {
        return crack;
    }

    public void setCrack(@Nullable GhostCrackLocation crack) {
        this.crack = crack;
        syncNeeded = true;
    }

    // ---- sync
    public boolean consumeSyncNeeded() {
        boolean needed = syncNeeded;
        syncNeeded = false;
        return needed;
    }

    public void markForSync() {
        syncNeeded = true;
    }

    public CompoundNBT toSyncNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Invited", writeInvited());
        nbt.putInt("PocketStacks", pocket.countStacks());
        return nbt;
    }

    public void fromSyncNBT(CompoundNBT nbt) {
        readInvited(nbt.getList("Invited", Constants.NBT.TAG_INT_ARRAY));
        clientPocketStacks = nbt.getInt("PocketStacks");
    }

    // ---- save
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Invited", writeInvited());
        nbt.put("Pocket", pocket.save());
        if (crack != null) {
            nbt.put("Crack", crack.toNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        readInvited(nbt.getList("Invited", Constants.NBT.TAG_INT_ARRAY));
        pocket.load(GhostPocket.listFrom(nbt, "Pocket"));
        crack = nbt.contains("Crack", Constants.NBT.TAG_COMPOUND) ? GhostCrackLocation.fromNBT(nbt.getCompound("Crack")) : null;
        syncNeeded = true;
    }

    /** copies everything (used when a player entity is recreated: respawn, return from the End) */
    public void copyFrom(GhostUserData other) {
        deserializeNBT(other.serializeNBT());
    }

    private ListNBT writeInvited() {
        ListNBT list = new ListNBT();
        for (UUID id : invited) {
            list.add(NBTUtil.createUUID(id));
        }
        return list;
    }

    private void readInvited(ListNBT list) {
        invited.clear();
        for (INBT tag : list) {
            invited.add(NBTUtil.loadUUID(tag));
        }
    }
}
