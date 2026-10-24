package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.ghost.room.GhostCracks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/** Shift variation of opening a crack: closes it. */
public class GhostCloseCrackAction extends StandAction {

    public GhostCloseCrackAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && user instanceof ServerPlayerEntity) {
            GhostCracks.close((ServerPlayerEntity) user);
        }
    }
}
