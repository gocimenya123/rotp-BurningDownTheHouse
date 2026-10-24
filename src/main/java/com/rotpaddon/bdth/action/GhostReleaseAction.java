package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.entity.GhostObjectEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/** Shift variation of grabbing: takes the last object out of the pocket and puts it down in front of the user, in whatever world they are. */
public class GhostReleaseAction extends StandAction {
    private static final double PLACE_DISTANCE = 1.8;

    public GhostReleaseAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user instanceof PlayerEntity && GhostUserData.get((PlayerEntity) user)
                .map(data -> data.getPocketStacks(user.level.isClientSide()) > 0).orElse(false)) {
            return ActionConditionResult.POSITIVE;
        }
        return conditionMessage("bdth_pocket_empty");
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (world.isClientSide() || !(user instanceof PlayerEntity)) {
            return;
        }
        GhostUserData.get((PlayerEntity) user).ifPresent(data -> {
            ItemStack stack = data.getPocket().takeLast();
            if (!stack.isEmpty()) {
                Vector3d look = user.getLookAngle();
                Vector3d horizontal = new Vector3d(look.x, 0, look.z).normalize();
                Vector3d pos = user.position().add(horizontal.scale(PLACE_DISTANCE));
                GhostObjectEntity.spawn(world, stack, pos, user.yRot + 180);
                world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.3F, 1.6F);
            }
        });
    }
}
