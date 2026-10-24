package com.rotpaddon.bdth.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.ghost.room.GhostRoomService;
import com.rotpaddon.bdth.init.InitEntities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * The crack in a wall that leads to the ghost room. Seen only by those who can see Stands, and only the owner
 * (and whoever the owner invited) knows what to do with it.
 * The same entity is the exit crack on the wall of the room itself ({@code roomExit}).
 */
public class GhostCrackEntity extends Entity {
    private static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.defineId(GhostCrackEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Boolean> ROOM_EXIT = EntityDataManager.defineId(GhostCrackEntity.class, DataSerializers.BOOLEAN);

    public GhostCrackEntity(EntityType<? extends GhostCrackEntity> type, World world) {
        super(type, world);
        this.noPhysics = true;
        setNoGravity(true);
    }

    /**
     * @param wall   the wall block the crack is in
     * @param normal the horizontal direction the wall faces (towards whoever opened the crack)
     */
    public static GhostCrackEntity spawn(World world, UUID owner, BlockPos wall, Direction normal, boolean roomExit) {
        GhostCrackEntity crack = new GhostCrackEntity(InitEntities.GHOST_CRACK.get(), world);
        crack.setOwnerId(owner);
        crack.entityData.set(ROOM_EXIT, roomExit);
        crack.moveTo(wall.getX() + 0.5 + normal.getStepX() * 0.52, wall.getY(), wall.getZ() + 0.5 + normal.getStepZ() * 0.52, normal.toYRot(), 0);
        world.addFreshEntity(crack);
        return crack;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER, Optional.empty());
        entityData.define(ROOM_EXIT, false);
    }

    @Nullable
    public UUID getOwnerId() {
        return entityData.get(OWNER).orElse(null);
    }

    public void setOwnerId(@Nullable UUID owner) {
        entityData.set(OWNER, Optional.ofNullable(owner));
    }

    public boolean isRoomExit() {
        return entityData.get(ROOM_EXIT);
    }

    public Direction getNormal() {
        return Direction.fromYRot(yRot);
    }

    /** the point in front of the crack where a person using it stands */
    public Vector3d getFrontPos() {
        Direction normal = getNormal();
        return new Vector3d(getX() + normal.getStepX() * 0.4, getY(), getZ() + normal.getStepZ() * 0.4);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && !isRoomExit() && tickCount % 20 == 0) {
            Direction normal = getNormal();
            BlockPos wall = new BlockPos(getX() - normal.getStepX() * 0.52, getY() + 0.5, getZ() - normal.getStepZ() * 0.52);
            if (level.getBlockState(wall).isAir()) {
                remove(); // the wall is gone, and the crack with it
            }
        }
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (level.isClientSide()) {
            return ActionResultType.SUCCESS;
        }
        GhostRoomService.useCrack(this, player);
        return ActionResultType.CONSUME;
    }

    @Override
    public boolean isPickable() {
        return !removed;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        UUID owner = getOwnerId();
        if (owner != null) {
            nbt.putUUID("Owner", owner);
        }
        nbt.putBoolean("RoomExit", isRoomExit());
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        setOwnerId(nbt.hasUUID("Owner") ? nbt.getUUID("Owner") : null);
        entityData.set(ROOM_EXIT, nbt.getBoolean("RoomExit"));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
