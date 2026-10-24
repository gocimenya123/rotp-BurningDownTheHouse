package com.rotpaddon.bdth.ghost.room;

import com.rotpaddon.bdth.capability.GhostCrackLocation;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.entity.GhostCrackEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Opening and closing the crack of a user. A user has one crack at a time: opening a new one closes the old. */
public final class GhostCracks {
    private GhostCracks() {}

    /** @return the crack entity, or null if the user has no crack to remove or opened none */
    public static GhostCrackEntity open(ServerPlayerEntity user, BlockPos wall, Direction normal) {
        close(user);
        GhostCrackEntity crack = GhostCrackEntity.spawn(user.level, user.getUUID(), wall, normal, false);
        GhostUserData.get(user).ifPresent(data -> data.setCrack(
                new GhostCrackLocation(user.level.dimension(), new Vector3d(crack.getX(), crack.getY(), crack.getZ()), normal)));
        return crack;
    }

    /** removes the user's crack (if its chunk is loaded) and forgets where it was */
    public static void close(ServerPlayerEntity user) {
        GhostUserData.get(user).ifPresent(data -> {
            GhostCrackLocation location = data.getCrack();
            if (location != null) {
                ServerWorld world = user.server.getLevel(location.dimension);
                if (world != null) {
                    AxisAlignedBB area = new AxisAlignedBB(location.pos, location.pos).inflate(2, 3, 2);
                    world.getEntitiesOfClass(GhostCrackEntity.class, area, 
                            crack -> !crack.isRoomExit() && user.getUUID().equals(crack.getOwnerId()))
                    .forEach(Entity::remove);
                }
                data.setCrack(null);
            }
        });
    }

    /** whether the wall block can hold a crack: a solid wall two blocks tall with open space in front of it */
    public static boolean canHoldCrack(World world, BlockPos wall, Direction normal) {
        for (int dy = 0; dy < 2; dy++) {
            BlockPos pos = wall.above(dy);
            if (!world.getBlockState(pos).isCollisionShapeFullBlock(world, pos) || !world.isEmptyBlock(pos.relative(normal))) {
                return false;
            }
        }
        return true;
    }

    /** a crack aimed at a wall block high above the floor is moved down so that it reaches the floor */
    public static BlockPos settleToFloor(World world, BlockPos wall, Direction normal) {
        for (int i = 0; i < 2; i++) {
            BlockPos below = wall.below();
            if (!world.getBlockState(below).isAir() && world.isEmptyBlock(below.relative(normal))) {
                wall = below;
            }
            else {
                break;
            }
        }
        return wall;
    }
}
