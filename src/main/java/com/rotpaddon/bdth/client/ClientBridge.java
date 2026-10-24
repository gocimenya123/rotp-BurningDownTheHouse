package com.rotpaddon.bdth.client;

import java.util.List;

import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.client.screen.GhostComputerScreen;
import com.rotpaddon.bdth.item.GhostBookItem;
import com.rotpaddon.bdth.network.packets.GhostSquashPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ReadBookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Everything the common code (items, packet handlers) needs from the client side, in one place,
 * so that those classes never touch client-only classes directly.
 */
public final class ClientBridge {
    private ClientBridge() {}

    public static void onGhostDataSync(CompoundNBT data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            GhostUserData.get(mc.player).ifPresent(cap -> cap.fromSyncNBT(data));
        }
    }

    public static void onSquash(GhostSquashPacket packet) {
        ClientSquash.start(packet.entityId, packet.normalX, packet.normalZ, packet.duration, packet.squashing);
    }

    public static void onComputerResult(List<ITextComponent> lines) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof GhostComputerScreen) {
            ((GhostComputerScreen) mc.screen).addResult(lines);
        }
    }

    public static void openGhostComputer() {
        Minecraft.getInstance().setScreen(new GhostComputerScreen());
    }

    /** a vanilla written book made on the spot out of translation keys */
    public static void openGhostBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundNBT tag = book.getOrCreateTag();
        tag.putString("title", "Ghost Book");
        tag.putString("author", "Emporio");
        ListNBT pages = new ListNBT();
        for (int i = 1; i <= GhostBookItem.PAGES; i++) {
            pages.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(
                    new TranslationTextComponent("book." + BdthAddon.MOD_ID + ".page" + i))));
        }
        tag.put("pages", pages);
        Minecraft.getInstance().setScreen(new ReadBookScreen(new ReadBookScreen.WrittenBookInfo(book)));
    }
}
