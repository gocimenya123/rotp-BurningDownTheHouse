package com.rotpaddon.bdth.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.capability.GhostUserData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/** Opens the pocket: storage for ghost objects of any size. It is the vanilla chest screen over the user's own pocket inventory. */
public class GhostPocketAction extends StandAction {

    public GhostPocketAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && user instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;
            GhostUserData.get(player).ifPresent(data -> player.openMenu(new SimpleNamedContainerProvider(
                    (id, inventory, p) -> ChestContainer.threeRows(id, inventory, data.getPocket()), 
                    new TranslationTextComponent("container.burningdownthehouse.pocket"))));
        }
    }
}
