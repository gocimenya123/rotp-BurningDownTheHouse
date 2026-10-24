package com.rotpaddon.bdth.entity;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.rotpaddon.bdth.event.GhostBulletPassThroughEvent;
import com.rotpaddon.bdth.init.InitEntities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

/**
 * A bullet of the ghost pistol, built on the mod's own projectile base.
 * It deals no damage and breaks no blocks; it passes through the living and shoves other ghost objects.
 */
public class GhostBulletEntity extends ModdedProjectileEntity {
    private final Set<Integer> passedThrough = new HashSet<>();

    public GhostBulletEntity(LivingEntity shooter, World world) {
        super(InitEntities.GHOST_BULLET.get(), shooter, world);
    }

    public GhostBulletEntity(EntityType<? extends GhostBulletEntity> type, World world) {
        super(type, world);
    }

    @Override
    public int ticksLifespan() {
        return 40;
    }

    @Override
    protected float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return -1; // breaks nothing
    }

    /** it is a ghost: ordinary people cannot see it (the base class hides "stand damage" projectiles from them) */
    @Override
    public boolean standDamage() {
        return true;
    }

    /** the only thing a ghost bullet can hit is another ghost object */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof GhostObjectEntity && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && isAlive()) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.15), 
                    e -> e.isAlive() && e != getOwner() && !e.isSpectator())) {
                if (passedThrough.add(target.getId())) {
                    GhostBulletPassThroughEvent event = new GhostBulletPassThroughEvent(this, target);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isHandled()) {
                        ((ServerWorld) level).sendParticles(ParticleTypes.END_ROD, 
                                getX(), getY(), getZ(), 6, 0.1, 0.1, 0.1, 0.03);
                    }
                }
            }
        }
    }

    /** never hurts anything (the base class would otherwise "hurt" the ghost object, which a punch-based pick-up would misread) */
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return false;
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {
        Entity target = result.getEntity();
        if (!level.isClientSide() && target instanceof GhostObjectEntity) {
            Vector3d push = getDeltaMovement().normalize().scale(0.6);
            target.setPos(target.getX() + push.x, target.getY(), target.getZ() + push.z);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), 
                    SoundEvents.GLASS_BREAK, SoundCategory.NEUTRAL, 0.4F, 1.8F);
            ((ServerWorld) level).sendParticles(ParticleTypes.CRIT, 
                    result.getLocation().x, result.getLocation().y, result.getLocation().z, 8, 0.1, 0.1, 0.1, 0.1);
        }
        super.onHitEntity(result);
    }

    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (!level.isClientSide()) {
            ((ServerWorld) level).sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(), 4, 0.05, 0.05, 0.05, 0.02);
        }
        super.breakProjectile(targetType, hitTarget);
    }
}
