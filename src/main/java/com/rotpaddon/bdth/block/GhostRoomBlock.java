package com.rotpaddon.bdth.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** A translucent, faintly glowing, unbreakable block that the ghost room is built from. */
public class GhostRoomBlock extends Block {

    public GhostRoomBlock() {
        super(AbstractBlock.Properties.of(Material.STONE)
                .strength(-1.0F, 3600000.0F)
                .noDrops()
                .noOcclusion()
                .lightLevel(state -> 6)
                .isValidSpawn((state, world, pos, type) -> false)
                .isViewBlocking((state, world, pos) -> false)
                .isSuffocating((state, world, pos) -> false));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }
}
