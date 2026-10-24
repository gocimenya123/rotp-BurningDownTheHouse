package com.rotpaddon.bdth.ghost;

import java.util.Locale;

/**
 * Every kind of object that has a ghost in the burnt-down room.
 * Each type is backed by exactly one item (see {@code InitItems}); the same item is what a ghost object entity
 * holds, so the item, the world entity and the pocket contents are always the same "object".
 */
public enum GhostObjectType {
    PIANO       ("ghost_piano",       Size.HUGE,   2.0F, 1.25F, false, GhostShapes.piano()),
    PIANO_BENCH ("ghost_piano_bench", Size.MEDIUM, 1.0F, 0.55F, false, GhostShapes.pianoBench()),
    BOOKSHELF   ("ghost_bookshelf",   Size.HUGE,   1.2F, 2.05F, false, GhostShapes.bookshelf()),
    ARMCHAIR    ("ghost_armchair",    Size.LARGE,  1.0F, 1.0F,  false, GhostShapes.armchair()),
    LAMP        ("ghost_lamp",        Size.LARGE,  0.5F, 1.7F,  false, GhostShapes.lamp()),
    PAINTING    ("ghost_painting",    Size.LARGE,  1.2F, 0.95F, false, GhostShapes.painting()),
    DESK        ("ghost_desk",        Size.HUGE,   1.3F, 0.8F,  false, GhostShapes.desk()),
    CHOCOLATE   ("ghost_chocolate",   Size.SMALL,  0.5F, 0.3F,  true,  GhostShapes.chocolate()),
    JUICE       ("ghost_juice",       Size.SMALL,  0.4F, 0.35F, true,  GhostShapes.juice()),
    BOOK        ("ghost_book",        Size.SMALL,  0.5F, 0.3F,  true,  GhostShapes.book()),
    COMPUTER    ("ghost_computer",    Size.MEDIUM, 0.6F, 0.5F,  true,  GhostShapes.computer()),
    PISTOL      ("ghost_pistol",      Size.SMALL,  0.4F, 0.3F,  true,  GhostShapes.pistol());

    public enum Size {
        SMALL, MEDIUM, LARGE, HUGE;

        public String translationKey() {
            return "tooltip.burningdownthehouse.size." + name().toLowerCase(Locale.ROOT);
        }
    }

    private final String itemId;
    private final Size size;
    private final float width;
    private final float height;
    private final boolean looseInRoom;
    private final GhostShape shape;

    GhostObjectType(String itemId, Size size, float width, float height, boolean looseInRoom, GhostShape shape) {
        this.itemId = itemId;
        this.size = size;
        this.width = width;
        this.height = height;
        this.looseInRoom = looseInRoom;
        this.shape = shape;
    }

    /** registry path of the item (and of the translation keys) of this object */
    public String getItemId() {
        return itemId;
    }

    public Size getSize() {
        return size;
    }

    /** hitbox width of the ghost object entity */
    public float getWidth() {
        return width;
    }

    /** hitbox height of the ghost object entity */
    public float getHeight() {
        return height;
    }

    /**
     * Loose objects are the ones that the room refills over time (food, books, the computer, the gun);
     * furniture is placed once, when the room is built.
     */
    public boolean isLooseInRoom() {
        return looseInRoom;
    }

    public GhostShape getShape() {
        return shape;
    }
}
