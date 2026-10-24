package com.rotpaddon.bdth.item;

import com.rotpaddon.bdth.ghost.GhostObjectType;

import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

/** A piece of the room's furniture: has no use of its own, so a plain right click on a block puts it down. */
public class GhostFurnitureItem extends GhostItem {

    public GhostFurnitureItem(GhostObjectType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return placeInWorld(context);
    }
}
