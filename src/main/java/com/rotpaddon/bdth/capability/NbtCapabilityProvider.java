package com.rotpaddon.bdth.capability;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

/** Attaches a self-serializing data object to an entity or a world as a capability. */
public class NbtCapabilityProvider<T extends INBTSerializable<CompoundNBT>> implements ICapabilitySerializable<CompoundNBT> {
    private final Supplier<Capability<T>> capability;
    private final LazyOptional<T> instance;

    /**
     * @param capability the capability instance getter (the static field is only filled in after registration, hence the supplier)
     * @param factory    creates the data object
     */
    public NbtCapabilityProvider(Supplier<Capability<T>> capability, NonNullSupplier<T> factory) {
        this.capability = capability;
        this.instance = LazyOptional.of(factory);
    }

    @Override
    public <C> LazyOptional<C> getCapability(Capability<C> cap, Direction side) {
        return capability.get().orEmpty(cap, instance);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return instance.orElseThrow(() -> new IllegalStateException("Capability data is not available")).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        instance.orElseThrow(() -> new IllegalStateException("Capability data is not available")).deserializeNBT(nbt);
    }
}
