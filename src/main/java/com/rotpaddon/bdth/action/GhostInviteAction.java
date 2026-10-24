package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.ghost.room.GhostRoomService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/** Invites a player into the room, or takes the invitation back. The invited can step through the owner's crack even when the owner is not in the room. */
public class GhostInviteAction extends StandAction {

    public GhostInviteAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity entity = target.getEntity();
        return entity instanceof PlayerEntity && entity != user ? ActionConditionResult.POSITIVE : conditionMessage("bdth_players_only");
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (world.isClientSide() || !(user instanceof PlayerEntity) || !(target.getEntity() instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity owner = (PlayerEntity) user;
        PlayerEntity guest = (PlayerEntity) target.getEntity();
        GhostUserData.get(owner).ifPresent(data -> {
            boolean invited = data.toggleInvited(guest.getUUID());
            owner.displayClientMessage(new TranslationTextComponent(
                    invited ? "message.burningdownthehouse.invited" : "message.burningdownthehouse.uninvited", guest.getDisplayName()), true);
            if (invited) {
                guest.displayClientMessage(new TranslationTextComponent("message.burningdownthehouse.you_are_invited", owner.getDisplayName()), true);
            }
            // the room keeps its own copy of the list: the crack works for guests while the owner is away
            if (owner instanceof ServerPlayerEntity) {
                GhostRoomService.syncInvitations((ServerPlayerEntity) owner);
            }
        });
    }

    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (power != null && target != null && power.getUser() instanceof PlayerEntity && target.getEntity() != null
                && GhostUserData.get((PlayerEntity) power.getUser()).map(data -> data.isInvited(target.getEntity().getUUID())).orElse(false)) {
            return key + ".revoke";
        }
        return key;
    }
}
