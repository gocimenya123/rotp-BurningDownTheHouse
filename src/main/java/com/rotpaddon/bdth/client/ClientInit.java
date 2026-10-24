package com.rotpaddon.bdth.client;

import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.client.render.GhostBulletRenderer;
import com.rotpaddon.bdth.client.render.GhostCrackRenderer;
import com.rotpaddon.bdth.client.render.GhostObjectRenderer;
import com.rotpaddon.bdth.init.InitBlocks;
import com.rotpaddon.bdth.init.InitEntities;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BdthAddon.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(InitEntities.GHOST_OBJECT.get(), GhostObjectRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(InitEntities.GHOST_CRACK.get(), GhostCrackRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(InitEntities.GHOST_BULLET.get(), GhostBulletRenderer::new);

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(InitBlocks.GHOST_WALL.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(InitBlocks.GHOST_FLOOR_A.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(InitBlocks.GHOST_FLOOR_B.get(), RenderType.translucent());
        });
    }
}
