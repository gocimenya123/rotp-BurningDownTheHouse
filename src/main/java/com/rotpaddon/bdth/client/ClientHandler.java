package com.rotpaddon.bdth.client;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector3f;
import com.rotpaddon.bdth.BdthAddon;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-side effects of passing through a crack: the body gets flat for everyone who sees it, and the user's own view closes to a slit. */
@Mod.EventBusSubscriber(modid = BdthAddon.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {

    /** the body is squashed along the crack's normal, as if pressed into the wall */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        ClientSquash.State state = ClientSquash.get(event.getPlayer().getId());
        if (state != null) {
            float thickness = state.thickness(event.getPartialRenderTick());
            if (thickness < 1) {
                MatrixStack matrix = event.getMatrixStack();
                float angle = (float) Math.atan2(state.normalX, state.normalZ);
                matrix.mulPose(Vector3f.YP.rotation(angle));
                matrix.scale(1, 1, thickness);
                matrix.mulPose(Vector3f.YP.rotation(-angle));
            }
        }
    }

    @SubscribeEvent
    public static void onFov(EntityViewRenderEvent.FOVModifier event) {
        PlayerEntity player = ClientUtil.getClientPlayer();
        ClientSquash.State state = player != null ? ClientSquash.get(player.getId()) : null;
        if (state != null) {
            event.setFOV(event.getFOV() * (0.6 + 0.4 * state.thickness((float) event.getRenderPartialTicks())));
        }
    }

    @SubscribeEvent
    public static void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        PlayerEntity player = ClientUtil.getClientPlayer();
        ClientSquash.State state = player != null ? ClientSquash.get(player.getId()) : null;
        if (state != null) {
            float thickness = state.thickness(event.getPartialTicks());
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            // black bars close in from both sides, leaving a slit
            int bar = (int) (width / 2 * (1 - thickness) * 0.97F);
            if (bar > 0) {
                AbstractGui.fill(event.getMatrixStack(), 0, 0, bar, height, 0xFF000000);
                AbstractGui.fill(event.getMatrixStack(), width - bar, 0, width, height, 0xFF000000);
            }
        }
    }
}
