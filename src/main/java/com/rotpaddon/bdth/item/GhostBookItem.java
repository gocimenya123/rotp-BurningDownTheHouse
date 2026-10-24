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

/** A ghost book, read with the vanilla book screen. The pages are translation keys. */
public class GhostBookItem extends GhostItem {
    public static final int PAGES = 4;

    public GhostBookItem(GhostObjectType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!GhostUtil.canTouchGhosts(player)) {
            return ActionResult.pass(stack);
        }
        if (world.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientBridge.openGhostBook());
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    @Nullable
    @Override
    protected ITextComponent getUsageHint() {
        return new TranslationTextComponent("tooltip.burningdownthehouse.book_hint").withStyle(TextFormatting.DARK_GRAY);
    }
}
