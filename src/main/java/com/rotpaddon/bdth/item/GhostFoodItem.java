package com.rotpaddon.bdth.item;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.ghost.GhostUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ghost food has a taste but nothing in it: it passes through the body, feeds nobody and is not used up.
 * The vanilla food component only exists so that the item plays the eating animation and sound.
 */
public class GhostFoodItem extends GhostItem {
    private static final int RE_EAT_COOLDOWN = 40;
    private final boolean drink;

    public GhostFoodItem(GhostObjectType type, boolean drink, Properties properties) {
        super(type, properties.food(new Food.Builder().nutrition(0).saturationMod(0F).alwaysEat().build()));
        this.drink = drink;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return drink ? UseAction.DRINK : UseAction.EAT;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!GhostUtil.canTouchGhosts(player)) {
            return ActionResult.pass(stack);
        }
        player.startUsingItem(hand);
        return ActionResult.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        // deliberately no super call: no hunger or saturation, and the item is not consumed
        if (!world.isClientSide() && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.displayClientMessage(new TranslationTextComponent("message.burningdownthehouse.taste." + getGhostType().getItemId())
                    .withStyle(TextFormatting.ITALIC), true);
            // the food falls out through the body
            ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.9, player.getZ(), 
                    8, 0.15, 0.3, 0.15, 0.02);
            ((ServerWorld) world).sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.1, player.getZ(), 
                    4, 0.15, 0.0, 0.15, 0.01);
            player.getCooldowns().addCooldown(this, RE_EAT_COOLDOWN);
        }
        return stack;
    }

    @Nullable
    @Override
    protected ITextComponent getUsageHint() {
        return new TranslationTextComponent("tooltip.burningdownthehouse.food_hint").withStyle(TextFormatting.DARK_GRAY);
    }
}
