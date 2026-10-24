package com.rotpaddon.bdth.item;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.entity.GhostBulletEntity;
import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.ghost.GhostUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/** The ghost pistol never runs out of ghost bullets. Its bullets pass through the living and only affect other ghosts. */
public class GhostPistolItem extends GhostItem {
    private static final int FIRE_COOLDOWN = 10;

    public GhostPistolItem(GhostObjectType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!GhostUtil.canTouchGhosts(player)) {
            return ActionResult.pass(stack);
        }
        if (!world.isClientSide()) {
            GhostBulletEntity bullet = new GhostBulletEntity(player, world);
            bullet.shootFromRotation(player, 2.5F, 0.0F);
            world.addFreshEntity(bullet);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.DISPENSER_LAUNCH, SoundCategory.PLAYERS, 0.6F, 1.6F);
        }
        player.getCooldowns().addCooldown(this, FIRE_COOLDOWN);
        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    @Nullable
    @Override
    protected ITextComponent getUsageHint() {
        return new TranslationTextComponent("tooltip.burningdownthehouse.pistol_hint").withStyle(TextFormatting.DARK_GRAY);
    }
}
