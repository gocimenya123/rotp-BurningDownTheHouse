package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.rotpaddon.bdth.entity.GhostCrackEntity;
import com.rotpaddon.bdth.ghost.GhostUtil;
import com.rotpaddon.bdth.ghost.room.GhostCracks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Opens the crack in the wall that is looked at: the way into the ghost room. */
public class GhostOpenCrackAction extends StandAction {

    public GhostOpenCrackAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (GhostUtil.isInGhostRoom(user)) {
            return conditionMessage("bdth_not_in_room");
        }
        Direction face = target.getFace();
        if (face == null || face.getAxis().isVertical()) {
            return conditionMessage("bdth_wall_only");
        }
        BlockPos wall = GhostCracks.settleToFloor(user.level, target.getBlockPos(), face);
        if (!GhostCracks.canHoldCrack(user.level, wall, face)) {
            return conditionMessage("bdth_no_space");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (world.isClientSide() || !(user instanceof ServerPlayerEntity)) {
            return;
        }
        Direction face = target.getFace();
        BlockPos wall = GhostCracks.settleToFloor(world, target.getBlockPos(), face);
        GhostCrackEntity crack = GhostCracks.open((ServerPlayerEntity) user, wall, face);
        world.playSound(null, crack.getX(), crack.getY() + 1, crack.getZ(), SoundEvents.GLASS_BREAK, SoundCategory.PLAYERS, 0.7F, 0.5F);
        ((ServerWorld) world).sendParticles(ParticleTypes.SOUL, crack.getX(), crack.getY() + 1, crack.getZ(), 12, 0.1, 0.8, 0.1, 0.02);
    }
}
