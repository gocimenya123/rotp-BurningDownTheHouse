package com.rotpaddon.bdth.ghost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure-data description of a ghost object's look: a list of coloured cuboids.
 * Coordinates are in blocks, relative to the object's bottom-centre; the front of the object faces -Z.
 * Used by the entity renderer (and nowhere else), so the ghosts need no binary model assets.
 */
public final class GhostShape {
    private final List<Box> boxes;

    private GhostShape(List<Box> boxes) {
        this.boxes = Collections.unmodifiableList(boxes);
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Box {
        public final float x0, y0, z0, x1, y1, z1;
        public final int r, g, b;
        /** emissive boxes ignore the world light level */
        public final boolean glow;

        private Box(float x0, float y0, float z0, float x1, float y1, float z1, int rgb, boolean glow) {
            this.x0 = Math.min(x0, x1);
            this.y0 = Math.min(y0, y1);
            this.z0 = Math.min(z0, z1);
            this.x1 = Math.max(x0, x1);
            this.y1 = Math.max(y0, y1);
            this.z1 = Math.max(z0, z1);
            this.r = (rgb >> 16) & 0xFF;
            this.g = (rgb >> 8) & 0xFF;
            this.b = rgb & 0xFF;
            this.glow = glow;
        }
    }

    public static final class Builder {
        private final List<Box> boxes = new ArrayList<>();

        public Builder box(float x0, float y0, float z0, float x1, float y1, float z1, int rgb) {
            boxes.add(new Box(x0, y0, z0, x1, y1, z1, rgb, false));
            return this;
        }

        public Builder glowBox(float x0, float y0, float z0, float x1, float y1, float z1, int rgb) {
            boxes.add(new Box(x0, y0, z0, x1, y1, z1, rgb, true));
            return this;
        }

        /** a box given by its centre (cx, cz), bottom (y) and size */
        public Builder column(float cx, float y, float cz, float width, float height, float depth, int rgb) {
            return box(cx - width / 2, y, cz - depth / 2, cx + width / 2, y + height, cz + depth / 2, rgb);
        }

        public GhostShape build() {
            return new GhostShape(new ArrayList<>(boxes));
        }
    }
}
