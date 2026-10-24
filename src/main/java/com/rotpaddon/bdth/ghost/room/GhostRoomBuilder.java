package com.rotpaddon.bdth.ghost.room;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.rotpaddon.bdth.entity.GhostCrackEntity;
import com.rotpaddon.bdth.entity.GhostObjectEntity;
import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.init.InitBlocks;
import com.rotpaddon.bdth.init.InitItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

/**
 * Builds the music room, procedurally (so there is no structure file to ship): a 17x9x17 shell of ghost blocks
 * with a checkered floor, the grand piano and the rest of the furniture, and the crack that leads back out.
 */
public final class GhostRoomBuilder {
    public static final int SIZE_XZ = 17;
    public static final int SIZE_Y = 9;
    /** how often the loose objects (food, books, the computer, the gun) are put back if they are gone */
    public static final long RESTOCK_PERIOD = 24000L;

    private static final int EXIT_X = 8;

    /** one object of the room: the position is relative to the lowest corner of the room */
    private static final class Placement {
        final GhostObjectType type;
        final double x, y, z;
        final float yaw;

        Placement(GhostObjectType type, double x, double y, double z, float yaw) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }
    }

    // yaw is the direction the front of the object faces: 0 south (+Z), 90 west, 180 north, 270 east
    private static final List<Placement> LAYOUT = Arrays.asList(
            new Placement(GhostObjectType.PIANO,       4.5, 1, 8.5, 270),
            new Placement(GhostObjectType.PIANO_BENCH, 6.6, 1, 8.5, 90),
            new Placement(GhostObjectType.BOOKSHELF,   15.4, 1, 5.0, 90),
            new Placement(GhostObjectType.BOOKSHELF,   15.4, 1, 11.5, 90),
            new Placement(GhostObjectType.ARMCHAIR,    10.5, 1, 13.0, 200),
            new Placement(GhostObjectType.LAMP,        2.0, 1, 13.5, 0),
            new Placement(GhostObjectType.PAINTING,    1.15, 2.6, 5.0, 270),
            new Placement(GhostObjectType.PAINTING,    4.5, 2.6, 1.15, 0),
            new Placement(GhostObjectType.PAINTING,    12.5, 2.6, 1.15, 0),
            new Placement(GhostObjectType.DESK,        13.0, 1, 3.2, 0),
            new Placement(GhostObjectType.COMPUTER,    13.0, 1.76, 3.2, 0),
            new Placement(GhostObjectType.CHOCOLATE,   11.9, 1.76, 3.2, 20),
            new Placement(GhostObjectType.JUICE,       14.1, 1.76, 3.2, 0),
            new Placement(GhostObjectType.BOOK,        12.0, 1.0, 5.5, 45),
            new Placement(GhostObjectType.PISTOL,      10.5, 1.45, 13.0, 90));

    private GhostRoomBuilder() {}

    public static AxisAlignedBB getBounds(BlockPos origin) {
        return new AxisAlignedBB(origin, origin.offset(SIZE_XZ, SIZE_Y, SIZE_XZ));
    }

    /** where whoever enters the room appears: in front of the exit crack, facing into the room */
    public static Vector3d getEntryPos(BlockPos origin) {
        return new Vector3d(origin.getX() + EXIT_X + 0.5, origin.getY() + 1, origin.getZ() + 2.0);
    }

    public static float getEntryYaw() {
        return Direction.SOUTH.toYRot();
    }

    public static void build(ServerWorld world, BlockPos origin, UUID owner) {
        // building blocks makes the (void) chunks generate; load them first so that the entities can be added
        for (int x = 0; x < SIZE_XZ; x += 8) {
            for (int z = 0; z < SIZE_XZ; z += 8) {
                world.getChunk((origin.getX() + x) >> 4, (origin.getZ() + z) >> 4);
            }
        }
        world.getChunk((origin.getX() + SIZE_XZ - 1) >> 4, (origin.getZ() + SIZE_XZ - 1) >> 4);

        BlockState wall = InitBlocks.GHOST_WALL.get().defaultBlockState();
        BlockState floorA = InitBlocks.GHOST_FLOOR_A.get().defaultBlockState();
        BlockState floorB = InitBlocks.GHOST_FLOOR_B.get().defaultBlockState();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = 0; x < SIZE_XZ; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_XZ; z++) {
                    boolean shell = x == 0 || z == 0 || y == 0 || x == SIZE_XZ - 1 || z == SIZE_XZ - 1 || y == SIZE_Y - 1;
                    BlockState state = !shell ? Blocks.AIR.defaultBlockState() 
                            : y == 0 ? ((x + z) % 2 == 0 ? floorA : floorB) : wall;
                    world.setBlock(pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z), state, 2);
                }
            }
        }

        GhostCrackEntity.spawn(world, owner, origin.offset(EXIT_X, 1, 0), Direction.SOUTH, true);
        for (Placement placement : LAYOUT) {
            spawn(world, origin, placement);
        }
    }

    /** Puts back the loose objects that are missing (taken out of the room); furniture is never refilled. */
    public static void restock(ServerWorld world, BlockPos origin) {
        AxisAlignedBB bounds = getBounds(origin);
        for (Placement placement : LAYOUT) {
            if (!placement.type.isLooseInRoom()) {
                continue;
            }
            boolean present = !world.getEntitiesOfClass(GhostObjectEntity.class, bounds, 
                    entity -> entity.getGhostType() == placement.type).isEmpty();
            if (!present) {
                spawn(world, origin, placement);
            }
        }
    }

    private static void spawn(ServerWorld world, BlockPos origin, Placement placement) {
        GhostObjectEntity.spawn(world, new ItemStack(InitItems.ghostItem(placement.type)), 
                new Vector3d(origin.getX() + placement.x, origin.getY() + placement.y, origin.getZ() + placement.z), placement.yaw);
    }
}
