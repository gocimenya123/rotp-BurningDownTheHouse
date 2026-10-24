package com.rotpaddon.bdth.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.capability.GhostPocket;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.ghost.GhostUtil;
import com.rotpaddon.bdth.init.InitEntities;
import com.rotpaddon.bdth.item.GhostItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * A ghost object standing in the world. It can be seen and touched only by those who can see Stands,
 * has no collision (living entities walk right through it) and keeps the ghost item it came from.
 */
public class GhostObjectEntity extends Entity {
    private static final DataParameter<ItemStack> STACK = EntityDataManager.defineId(GhostObjectEntity.class, DataSerializers.ITEM_STACK);
    private static final int PASS_THROUGH_PERIOD = 10;

    public GhostObjectEntity(EntityType<? extends GhostObjectEntity> type, World world) {
        super(type, world);
        this.noPhysics = true;
        setNoGravity(true);
    }

    public static GhostObjectEntity spawn(World world, ItemStack stack, Vector3d pos, float yRot) {
        GhostObjectEntity entity = new GhostObjectEntity(InitEntities.GHOST_OBJECT.get(), world);
        ItemStack held = stack.copy();
        held.setCount(1);
        entity.setStack(held);
        entity.moveTo(pos.x, pos.y, pos.z, yRot, 0);
        world.addFreshEntity(entity);
        return entity;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(STACK, ItemStack.EMPTY);
    }

    public ItemStack getStack() {
        return entityData.get(STACK);
    }

    public void setStack(ItemStack stack) {
        entityData.set(STACK, stack);
        refreshDimensions();
    }

    @Nullable
    public GhostObjectType getGhostType() {
        ItemStack stack = getStack();
        return stack.getItem() instanceof GhostItem ? ((GhostItem) stack.getItem()).getGhostType() : null;
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        super.onSyncedDataUpdated(key);
        if (STACK.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    public EntitySize getDimensions(Pose pose) {
        GhostObjectType type = getGhostType();
        return type != null ? EntitySize.scalable(type.getWidth(), type.getHeight()) : super.getDimensions(pose);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            if (getStack().isEmpty() || getGhostType() == null) {
                remove();
                return;
            }
            if (tickCount % PASS_THROUGH_PERIOD == 0) {
                spawnPassThroughEffect();
            }
        }
    }

    /** the ripple that shows a living thing walking through the ghost */
    private void spawnPassThroughEffect() {
        List<LivingEntity> inside = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().deflate(0.05), 
                entity -> entity.isAlive() && !entity.isSpectator());
        if (!inside.isEmpty()) {
            ((ServerWorld) level).sendParticles(ParticleTypes.END_ROD, 
                    inside.get(0).getX(), inside.get(0).getY() + inside.get(0).getBbHeight() / 2, inside.get(0).getZ(), 
                    3, 0.2, 0.3, 0.2, 0.01);
        }
    }

    // ---- no collision: everything passes through
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !removed;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    /** A punch from someone who can touch ghosts takes the object; nothing else can hurt it. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (!level.isClientSide() && attacker instanceof PlayerEntity && GhostUtil.canTouchGhosts((PlayerEntity) attacker)) {
            pickUp((PlayerEntity) attacker);
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d hitVec, Hand hand) {
        if (!GhostUtil.canTouchGhosts(player)) {
            return ActionResultType.PASS;
        }
        if (level.isClientSide()) {
            return ActionResultType.SUCCESS;
        }
        GhostObjectType type = getGhostType();
        if (player.isShiftKeyDown() || type != GhostObjectType.PIANO) {
            pickUp(player);
        }
        else {
            playPianoKey(hitVec);
        }
        return ActionResultType.CONSUME;
    }

    /** the piano is played by clicking along its keyboard; the position of the click picks the note */
    private void playPianoKey(Vector3d hitVec) {
        Vector3d local = hitVec.yRot((float) Math.toRadians(yRot));
        int note = MathHelper.clamp((int) ((local.x + 0.7) / 1.4 * 24), 0, 24);
        float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
        level.playSound(null, getX(), getY() + 0.6, getZ(), SoundEvents.NOTE_BLOCK_HARP, SoundCategory.RECORDS, 2.0F, pitch);
        ((ServerWorld) level).sendParticles(ParticleTypes.NOTE, getX() + local.x, getY() + 1.1, getZ(), 
                1, 0, 0, 0, note / 24.0);
    }

    /** Puts the object into the inventory (one slot, whatever its size); the pocket takes it if the inventory is full. */
    public void pickUp(PlayerEntity player) {
        ItemStack stack = getStack().copy();
        boolean taken = player.inventory.add(stack);
        if (!taken) {
            ItemStack left = stack;
            taken = GhostUserData.get(player).map(data -> {
                GhostPocket pocket = data.getPocket();
                return pocket.addItem(left).isEmpty();
            }).orElse(false);
        }
        if (taken) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.4F, 1.4F);
            remove();
        }
        else {
            player.displayClientMessage(new TranslationTextComponent("message.burningdownthehouse.no_room"), true);
        }
    }

    // ---- save / network
    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.put("Item", getStack().save(new CompoundNBT()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        setStack(ItemStack.of(nbt.getCompound("Item")));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
