package com.rotpaddon.bdth.init;

import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.block.GhostRoomBlock;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BdthAddon.MOD_ID);

    public static final RegistryObject<Block> GHOST_WALL = BLOCKS.register("ghost_wall", GhostRoomBlock::new);
    public static final RegistryObject<Block> GHOST_FLOOR_A = BLOCKS.register("ghost_floor_a", GhostRoomBlock::new);
    public static final RegistryObject<Block> GHOST_FLOOR_B = BLOCKS.register("ghost_floor_b", GhostRoomBlock::new);
}
