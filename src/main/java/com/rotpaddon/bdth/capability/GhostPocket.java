package com.rotpaddon.bdth.capability;

import com.rotpaddon.bdth.item.GhostItem;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

/**
 * The "pocket": extra storage that takes ghost objects of any size, on top of the regular inventory.
 * It is opened with the vanilla chest container, so no custom GUI or container sync is needed.
 */
public class GhostPocket extends Inventory {
    public static final int SIZE = 27;

    public GhostPocket() {
        super(SIZE);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof GhostItem;
    }

    public int countStacks() {
        int count = 0;
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /** removes and returns the last stored object, or an empty stack */
    public ItemStack takeLast() {
        for (int i = getContainerSize() - 1; i >= 0; i--) {
            if (!getItem(i).isEmpty()) {
                ItemStack taken = removeItemNoUpdate(i);
                setChanged();
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public ListNBT save() {
        ListNBT list = new ListNBT();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                CompoundNBT slotNbt = new CompoundNBT();
                slotNbt.putByte("Slot", (byte) i);
                stack.save(slotNbt);
                list.add(slotNbt);
            }
        }
        return list;
    }

    public void load(ListNBT list) {
        clearContent();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT slotNbt = list.getCompound(i);
            int slot = slotNbt.getByte("Slot") & 0xFF;
            if (slot < getContainerSize()) {
                setItem(slot, ItemStack.of(slotNbt));
            }
        }
    }

    public static ListNBT listFrom(CompoundNBT nbt, String key) {
        return nbt.getList(key, Constants.NBT.TAG_COMPOUND);
    }
}
