package com.rotpaddon.bdth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.ghost.room.GhostRoomService;
import com.rotpaddon.bdth.ghost.room.GhostRoomWorldData;
import com.rotpaddon.bdth.ghost.room.GhostTransitions;
import com.rotpaddon.bdth.init.InitBlocks;
import com.rotpaddon.bdth.init.InitEntities;
import com.rotpaddon.bdth.init.InitItems;
import com.rotpaddon.bdth.init.InitStands;
import com.rotpaddon.bdth.network.BdthNetwork;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Burning Down the House (Stone Ocean, Part 6) - a Ripples of the Past addon.
 * <p>
 * The Stand has no body: it is a pure ability that opens a crack into the "ghost" of a burnt-down music room
 * and lets its user carry, use and hide among the ghosts of objects.
 */
@Mod(BdthAddon.MOD_ID)
public class BdthAddon {
    public static final String MOD_ID = "burningdownthehouse";
    public static final Logger LOGGER = LogManager.getLogger();

    public BdthAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        InitBlocks.BLOCKS.register(modEventBus);
        InitItems.ITEMS.register(modEventBus);
        InitEntities.ENTITIES.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);

        // static @SubscribeEvent handlers of the Forge bus (BdthEventHandler registers itself with @EventBusSubscriber)
        MinecraftForge.EVENT_BUS.register(GhostRoomService.class);
        MinecraftForge.EVENT_BUS.register(GhostTransitions.class);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BdthNetwork.init();
            GhostUserData.registerCapability();
            GhostRoomWorldData.registerCapability();
        });
    }
}
