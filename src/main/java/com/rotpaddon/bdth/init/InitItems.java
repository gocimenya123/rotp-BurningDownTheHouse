package com.rotpaddon.bdth.init;

import java.util.EnumMap;
import java.util.Map;

import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.ghost.GhostObjectType;
import com.rotpaddon.bdth.item.GhostBookItem;
import com.rotpaddon.bdth.item.GhostComputerItem;
import com.rotpaddon.bdth.item.GhostFoodItem;
import com.rotpaddon.bdth.item.GhostFurnitureItem;
import com.rotpaddon.bdth.item.GhostItem;
import com.rotpaddon.bdth.item.GhostPistolItem;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BdthAddon.MOD_ID);

    public static final ItemGroup TAB = new ItemGroup(BdthAddon.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ghostItem(GhostObjectType.PIANO));
        }
    };

    private static final Map<GhostObjectType, RegistryObject<GhostItem>> GHOST_ITEMS = new EnumMap<>(GhostObjectType.class);
    static {
        for (GhostObjectType type : GhostObjectType.values()) {
            GHOST_ITEMS.put(type, ITEMS.register(type.getItemId(), () -> createGhostItem(type, new Item.Properties().tab(TAB))));
        }
    }

    private static GhostItem createGhostItem(GhostObjectType type, Item.Properties properties) {
        switch (type) {
        case CHOCOLATE: return new GhostFoodItem(type, false, properties);
        case JUICE:     return new GhostFoodItem(type, true, properties);
        case BOOK:      return new GhostBookItem(type, properties);
        case COMPUTER:  return new GhostComputerItem(type, properties);
        case PISTOL:    return new GhostPistolItem(type, properties);
        default:        return new GhostFurnitureItem(type, properties);
        }
    }

    public static GhostItem ghostItem(GhostObjectType type) {
        return GHOST_ITEMS.get(type).get();
    }

    public static final RegistryObject<Item> GHOST_WALL = blockItem("ghost_wall", InitBlocks.GHOST_WALL);
    public static final RegistryObject<Item> GHOST_FLOOR_A = blockItem("ghost_floor_a", InitBlocks.GHOST_FLOOR_A);
    public static final RegistryObject<Item> GHOST_FLOOR_B = blockItem("ghost_floor_b", InitBlocks.GHOST_FLOOR_B);

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(TAB)));
    }
}
