package com.rotpaddon.bdth.ghost;

/**
 * The cuboid shapes of every ghost object (see {@link GhostShape}).
 * Colours were picked to match the music room from the manga: a black grand piano, dark wood, warm lamp light.
 */
final class GhostShapes {
    private static final int BLACK = 0x15151A;
    private static final int KEY_WHITE = 0xEDEDE6;
    private static final int WOOD_DARK = 0x4A3222;
    private static final int WOOD_MID = 0x6E4A30;
    private static final int WINE = 0x6B2630;
    private static final int METAL = 0x555A63;
    private static final int BEIGE = 0xCFC8B4;

    private GhostShapes() {}

    static GhostShape piano() {
        GhostShape.Builder b = GhostShape.builder();
        // legs
        b.column(-0.70F, 0, -0.45F, 0.10F, 0.55F, 0.10F, BLACK);
        b.column(0.70F, 0, -0.45F, 0.10F, 0.55F, 0.10F, BLACK);
        b.column(0.00F, 0, 0.75F, 0.10F, 0.55F, 0.10F, BLACK);
        // case, curved tail approximated by a narrower box
        b.box(-0.80F, 0.55F, -0.60F, 0.80F, 0.80F, 0.80F, BLACK);
        b.box(-0.55F, 0.55F, 0.80F, 0.55F, 0.80F, 1.00F, BLACK);
        // propped-open lid
        b.box(-0.78F, 0.80F, -0.20F, 0.78F, 0.84F, 0.80F, 0x23232B);
        b.box(-0.78F, 0.84F, 0.60F, 0.78F, 1.20F, 0.64F, 0x23232B);
        // keyboard
        b.box(-0.70F, 0.55F, -0.95F, 0.70F, 0.65F, -0.60F, KEY_WHITE);
        for (int i = 0; i < 12; i++) {
            if (i % 7 == 2 || i % 7 == 6) continue; // the gaps of an octave
            float x = -0.62F + i * 0.113F;
            b.box(x - 0.02F, 0.65F, -0.95F, x + 0.02F, 0.69F, -0.78F, BLACK);
        }
        // music stand and pedals
        b.box(-0.30F, 0.80F, -0.58F, 0.30F, 1.00F, -0.52F, 0x2B2B33);
        b.box(-0.12F, 0.0F, -0.55F, 0.12F, 0.05F, -0.40F, 0xB59A55);
        return b.build();
    }

    static GhostShape pianoBench() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.45F, 0.40F, -0.20F, 0.45F, 0.52F, 0.20F, WINE);
        float[] xs = {-0.40F, 0.40F};
        float[] zs = {-0.15F, 0.15F};
        for (float x : xs) {
            for (float z : zs) {
                b.column(x, 0, z, 0.06F, 0.40F, 0.06F, BLACK);
            }
        }
        return b.build();
    }

    static GhostShape bookshelf() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.55F, 0, 0.12F, 0.55F, 2.0F, 0.18F, WOOD_DARK); // back
        b.box(-0.55F, 0, -0.20F, -0.50F, 2.0F, 0.18F, WOOD_DARK);
        b.box(0.50F, 0, -0.20F, 0.55F, 2.0F, 0.18F, WOOD_DARK);
        int[] bookColors = {0x7A2E2E, 0x2E4F7A, 0x3E6B3A, 0x8A6A2A, 0x5A3A6B};
        for (int shelf = 0; shelf < 4; shelf++) {
            float y = 0.04F + shelf * 0.62F;
            b.box(-0.55F, y, -0.20F, 0.55F, y + 0.04F, 0.18F, WOOD_MID);
            if (shelf == 3) break;
            for (int i = 0; i < 9; i++) {
                float h = 0.38F + ((i * 7 + shelf * 3) % 4) * 0.04F;
                b.box(-0.46F + i * 0.10F, y + 0.04F, -0.12F, -0.38F + i * 0.10F, y + 0.04F + h, 0.10F,
                        bookColors[(i + shelf) % bookColors.length]);
            }
        }
        return b.build();
    }

    static GhostShape armchair() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.40F, 0.15F, -0.40F, 0.40F, 0.45F, 0.40F, WINE);
        b.box(-0.40F, 0.45F, 0.28F, 0.40F, 1.00F, 0.40F, WINE);
        b.box(-0.48F, 0.15F, -0.40F, -0.40F, 0.65F, 0.40F, 0x55202A);
        b.box(0.40F, 0.15F, -0.40F, 0.48F, 0.65F, 0.40F, 0x55202A);
        b.column(-0.36F, 0, -0.36F, 0.08F, 0.15F, 0.08F, BLACK);
        b.column(0.36F, 0, -0.36F, 0.08F, 0.15F, 0.08F, BLACK);
        b.column(-0.36F, 0, 0.36F, 0.08F, 0.15F, 0.08F, BLACK);
        b.column(0.36F, 0, 0.36F, 0.08F, 0.15F, 0.08F, BLACK);
        return b.build();
    }

    static GhostShape lamp() {
        GhostShape.Builder b = GhostShape.builder();
        b.column(0, 0, 0, 0.30F, 0.04F, 0.30F, BLACK);
        b.column(0, 0.04F, 0, 0.05F, 1.35F, 0.05F, 0xB59A55);
        b.glowBox(-0.20F, 1.30F, -0.20F, 0.20F, 1.65F, 0.20F, 0xFFD98A);
        return b.build();
    }

    static GhostShape painting() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.60F, 0, -0.03F, 0.60F, 0.90F, 0.03F, 0xB59A55);
        b.box(-0.54F, 0.06F, -0.04F, 0.54F, 0.84F, 0.04F, 0x2E4F7A);
        b.glowBox(-0.54F, 0.06F, -0.045F, 0.54F, 0.30F, 0.0F, 0x7FB0D8);
        b.box(-0.30F, 0.30F, -0.045F, 0.10F, 0.62F, 0.0F, 0x4A7A4A);
        return b.build();
    }

    static GhostShape desk() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.60F, 0.70F, -0.30F, 0.60F, 0.76F, 0.30F, WOOD_MID);
        b.column(-0.55F, 0, -0.25F, 0.07F, 0.70F, 0.07F, WOOD_DARK);
        b.column(0.55F, 0, -0.25F, 0.07F, 0.70F, 0.07F, WOOD_DARK);
        b.column(-0.55F, 0, 0.25F, 0.07F, 0.70F, 0.07F, WOOD_DARK);
        b.column(0.55F, 0, 0.25F, 0.07F, 0.70F, 0.07F, WOOD_DARK);
        b.box(0.20F, 0.45F, -0.28F, 0.58F, 0.70F, 0.28F, WOOD_DARK);
        return b.build();
    }

    static GhostShape chocolate() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.10F, 0, -0.16F, 0.10F, 0.03F, 0.16F, 0x4B2A1A);
        for (int i = 0; i < 4; i++) {
            b.box(-0.09F, 0.03F, -0.15F + i * 0.075F, 0.09F, 0.04F, -0.095F + i * 0.075F, 0x5E3622);
        }
        b.box(-0.105F, 0, 0.04F, 0.105F, 0.035F, 0.16F, 0xC9A24A); // foil wrapper
        return b.build();
    }

    static GhostShape juice() {
        GhostShape.Builder b = GhostShape.builder();
        b.column(0, 0, 0, 0.12F, 0.20F, 0.12F, 0xE8E4D2);
        b.column(0, 0.14F, 0, 0.125F, 0.04F, 0.125F, 0xD7742B);
        b.box(-0.04F, 0.20F, -0.04F, 0.04F, 0.24F, 0.04F, 0xE8E4D2);
        return b.build();
    }

    static GhostShape book() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.10F, 0, -0.14F, 0.10F, 0.05F, 0.14F, 0x7A2E2E);
        b.box(-0.09F, 0.005F, -0.13F, 0.10F, 0.045F, 0.13F, 0xEDE3C8);
        return b.build();
    }

    static GhostShape computer() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.22F, 0, -0.12F, 0.22F, 0.05F, 0.12F, BEIGE);
        b.box(-0.10F, 0.05F, 0.0F, 0.10F, 0.10F, 0.06F, BEIGE);
        b.box(-0.19F, 0.10F, 0.0F, 0.19F, 0.40F, 0.12F, BEIGE);
        b.glowBox(-0.155F, 0.14F, -0.01F, 0.155F, 0.36F, 0.0F, 0x7FE8D8);
        return b.build();
    }

    static GhostShape pistol() {
        GhostShape.Builder b = GhostShape.builder();
        b.box(-0.025F, 0.07F, -0.16F, 0.025F, 0.12F, 0.10F, METAL);
        b.box(-0.03F, 0.0F, 0.04F, 0.03F, 0.08F, 0.10F, 0x2B2B33);
        b.box(-0.02F, 0.04F, -0.01F, 0.02F, 0.07F, 0.02F, METAL);
        return b.build();
    }
}
