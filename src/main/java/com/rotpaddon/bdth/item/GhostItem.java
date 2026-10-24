package com.rotpaddon.bdth.item;

import java.util.List;

import javax.annotation.Nullable;

import com.rotpaddon.bdth.entity.GhostObjectEntity;
import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.ghost.GhostUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Base of every ghost item. A ghost item is a normal item in the inventory (one slot, whatever the size of the object),
 * that only those who can see Stands can touch, and that turns back into a {@link GhostObjectEntity} when put into the world.
 */
public abstract class GhostItem extends Item {
    private final GhostObjectType type;

    protected GhostItem(GhostObjectType type, Item.Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public GhostObjectType getGhostType() {
        return type;
    }

    /** the ghosts glow in the inventory like enchanted items do */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.burningdownthehouse.ghost_item").withStyle(TextFormatting.AQUA, TextFormatting.ITALIC));
        tooltip.add(new TranslationTextComponent("tooltip.burningdownthehouse.size", 
                new TranslationTextComponent(type.getSize().translationKey())).withStyle(TextFormatting.GRAY));
        ITextComponent hint = getUsageHint();
        if (hint != null) {
            tooltip.add(hint);
        }
    }

    @Nullable
    protected ITextComponent getUsageHint() {
        return null;
    }

    /** Sneak + right click on a block puts any ghost object there (items with their own "use" keep the plain right click). */
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return ActionResultType.PASS;
        }
        return placeInWorld(context);
    }

    protected ActionResultType placeInWorld(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getLevel();
        if (player == null || !GhostUtil.canTouchGhosts(player)) {
            if (player != null && !world.isClientSide()) {
                player.displayClientMessage(new TranslationTextComponent("message.burningdownthehouse.cant_touch"), true);
            }
            return ActionResultType.PASS;
        }
        if (!world.isClientSide()) {
            Direction face = context.getClickedFace();
            Vector3d pos = context.getClickLocation();
            float halfWidth = type.getWidth() / 2;
            switch (face) {
            case DOWN:
                pos = pos.add(0, -type.getHeight(), 0);
                break;
            case UP:
                break;
            default:
                pos = pos.add(face.getStepX() * halfWidth, -type.getHeight() / 2, face.getStepZ() * halfWidth);
                break;
            }
            ItemStack placed = context.getItemInHand().copy();
            placed.setCount(1);
            float yaw = face.getAxis().isHorizontal() ? face.toYRot() : context.getRotation() + 180;
            GhostObjectEntity.spawn(world, placed, pos, yaw);
            if (!player.abilities.instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
    }
}
