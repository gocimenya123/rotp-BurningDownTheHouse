package com.rotpaddon.bdth.item;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.client.ClientBridge;
import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.ghost.GhostUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** The ghost computer: opens a terminal that answers questions (see {@code GhostComputerService}). */
public class GhostComputerItem extends GhostItem {

    public GhostComputerItem(GhostObjectType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!GhostUtil.canTouchGhosts(player)) {
            return ActionResult.pass(stack);
        }
        if (world.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientBridge.openGhostComputer());
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    @Nullable
    @Override
    protected ITextComponent getUsageHint() {
        return new TranslationTextComponent("tooltip.burningdownthehouse.computer_hint").withStyle(TextFormatting.DARK_GRAY);
    }
}
