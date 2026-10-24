package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import java.util.UUID;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.entity.GhostCrackEntity;
import com.rotpaddon.bdth.ghost.GhostUtil;
import com.rotpaddon.bdth.ghost.room.GhostRoomService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/** Steps into the crack that is close by, or, inside the room, back out of it. */
public class GhostEnterRoomAction extends StandAction {
    private static final double CRACK_REACH = 3.0;

    public GhostEnterRoomAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (!(user instanceof PlayerEntity)) {
            return ActionConditionResult.NEGATIVE;
        }
        if (GhostUtil.isInGhostRoom(user)) {
            return ActionConditionResult.POSITIVE;
        }
        PlayerEntity player = (PlayerEntity) user;
        boolean hasCrack = GhostRoomService.findCrackNear(player, player.getUUID(), CRACK_REACH) != null;
        if (!hasCrack) {
            hasCrack = user instanceof ServerPlayerEntity
                    ? GhostRoomService.findEnterableCrack((ServerPlayerEntity) user, CRACK_REACH) != null
                    // on the client the invitation cannot be checked, so any crack will do here
                    : GhostRoomService.findAnyCrackNear(player, CRACK_REACH) != null;
        }
        return hasCrack ? ActionConditionResult.POSITIVE : conditionMessage("bdth_no_crack");
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (world.isClientSide() || !(user instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) user;
        if (GhostUtil.isInGhostRoom(player)) {
            GhostRoomService.leave(player, Direction.SOUTH);
        }
        else {
            // the player's own crack first; otherwise a crack of someone who invited them
            GhostCrackEntity crack = GhostRoomService.findCrackNear(player, player.getUUID(), CRACK_REACH);
            if (crack == null) {
                crack = GhostRoomService.findEnterableCrack(player, CRACK_REACH);
            }
            if (crack != null) {
                UUID owner = crack.getOwnerId();
                if (owner != null && GhostRoomService.canEnter(player.server, owner, player)) {
                    // the destination room is the one of whoever owns the crack
                    GhostRoomService.enter(player, owner, crack);
                }
                else {
                    player.displayClientMessage(new TranslationTextComponent(
                            "message.burningdownthehouse.crack_unknown"), true);
                }
            }
            else {
                player.displayClientMessage(new TranslationTextComponent(
                        "message.burningdownthehouse.crack_unknown"), true);
            }
        }
    }

    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        return power != null && GhostUtil.isInGhostRoom(power.getUser()) ? key + ".leave" : key;
    }
}
