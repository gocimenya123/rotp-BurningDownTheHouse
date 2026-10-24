package com.rotpaddon.bdth.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

/** One storage for every capability of this addon: the data class serializes itself. */
public class NbtCapabilityStorage<T extends INBTSerializable<CompoundNBT>> implements Capability.IStorage<T> {

    @Override
    public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    }
}
