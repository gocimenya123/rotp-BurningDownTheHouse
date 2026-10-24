package com.rotpaddon.bdth.init;

import com.rotpaddon.bdth.BdthAddon;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/** The dimension itself is defined by the data pack files in data/burningdownthehouse/dimension(_type). */
public class InitDimensions {
    public static final RegistryKey<World> GHOST_ROOM = RegistryKey.create(
            Registry.DIMENSION_REGISTRY, new ResourceLocation(BdthAddon.MOD_ID, "ghost_room"));
}
