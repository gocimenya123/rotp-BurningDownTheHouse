package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.capability.GhostPocket;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.entity.GhostObjectEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

/** Takes the ghost object that is looked at straight into the pocket, however big it is. */
public class GhostGrabAction extends StandAction {

    public GhostGrabAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        return target.getEntity() instanceof GhostObjectEntity ? ActionConditionResult.POSITIVE : conditionMessage("bdth_ghost_only");
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user instanceof PlayerEntity && GhostUserData.get((PlayerEntity) user)
                .map(data -> data.getPocketStacks(user.level.isClientSide()) >= GhostPocket.SIZE).orElse(true)) {
            return conditionMessage("bdth_pocket_full");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        Entity entity = target.getEntity();
        if (world.isClientSide() || !(user instanceof PlayerEntity) || !(entity instanceof GhostObjectEntity)) {
            return;
        }
        GhostObjectEntity ghost = (GhostObjectEntity) entity;
        GhostUserData.get((PlayerEntity) user).ifPresent(data -> {
            if (data.getPocket().addItem(ghost.getStack().copy()).isEmpty()) {
                world.playSound(null, ghost.getX(), ghost.getY(), ghost.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.3F, 2.0F);
                ghost.remove();
            }
        });
    }
}
