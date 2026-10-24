package com.rotpaddon.bdth.capability;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/** Where a user's crack in the normal world is: the dimension, the wall position and the direction the wall faces. */
public class GhostCrackLocation {
    public final RegistryKey<World> dimension;
    public final Vector3d pos;
    public final Direction normal;

    public GhostCrackLocation(RegistryKey<World> dimension, Vector3d pos, Direction normal) {
        this.dimension = dimension;
        this.pos = pos;
        this.normal = normal;
    }

    /** the point just in front of the crack, where something leaving the room appears */
    public Vector3d exitPos() {
        return pos.add(normal.getStepX() * 0.9, 0, normal.getStepZ() * 0.9);
    }

    /** the yaw an entity leaving through the crack should face (away from the wall) */
    public float exitYaw() {
        return normal.toYRot();
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("Dim", dimension.location().toString());
        nbt.putDouble("X", pos.x);
        nbt.putDouble("Y", pos.y);
        nbt.putDouble("Z", pos.z);
        nbt.putByte("Normal", (byte) normal.get3DDataValue());
        return nbt;
    }

    @Nullable
    public static GhostCrackLocation fromNBT(CompoundNBT nbt) {
        if (!nbt.contains("Dim")) {
            return null;
        }
        ResourceLocation dimId = ResourceLocation.tryParse(nbt.getString("Dim"));
        if (dimId == null) {
            return null;
        }
        Direction normal = Direction.from3DDataValue(nbt.getByte("Normal"));
        if (normal.getAxis().isVertical()) {
            return null;
        }
        return new GhostCrackLocation(RegistryKey.create(Registry.DIMENSION_REGISTRY, dimId),
                new Vector3d(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z")), normal);
    }
}
