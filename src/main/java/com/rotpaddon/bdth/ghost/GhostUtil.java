package com.rotpaddon.bdth.ghost;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.rotpaddon.bdth.init.InitDimensions;
import com.rotpaddon.bdth.init.InitStands;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Who can perceive and touch ghosts.
 * Ghosts are seen and felt exactly by those who can see Stands in Ripples of the Past
 * (Stand users and whoever has Spirit Vision), so the mod's own rule is reused instead of a new one.
 */
public final class GhostUtil {
    private GhostUtil() {}

    public static boolean isBurningDownTheHouseUser(LivingEntity entity) {
        return IStandPower.getStandPowerOptional(entity)
                .map(power -> power.hasPower() && power.getType() == InitStands.BURNING_DOWN_THE_HOUSE.get())
                .orElse(false);
    }

    public static boolean canTouchGhosts(LivingEntity entity) {
        return entity instanceof PlayerEntity && StandUtil.playerCanSeeStands((PlayerEntity) entity);
    }

    public static boolean isInGhostRoom(Entity entity) {
        return entity.level.dimension() == InitDimensions.GHOST_ROOM;
    }
}
