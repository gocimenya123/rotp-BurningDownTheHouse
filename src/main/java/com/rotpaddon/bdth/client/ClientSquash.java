package com.rotpaddon.bdth.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.rotpaddon.bdth.BdthAddon;

/**
 * Client state of bodies being squeezed through a crack: how flat each entity currently is.
 * A squashing body stays flat until the server moves it (or a safety timeout runs out), then it expands on the other side.
 */
@Mod.EventBusSubscriber(modid = BdthAddon.MOD_ID, value = Dist.CLIENT)
public final class ClientSquash {
    private static final float MIN_THICKNESS = 0.05F;
    private static final int SAFETY_TIMEOUT = 60;
    private static final Map<Integer, State> STATES = new HashMap<>();
    private static int clientTicks = 0;

    private ClientSquash() {}

    public static class State {
        final int startTick;
        final int duration;
        final boolean squashing;
        public final int normalX;
        public final int normalZ;

        State(int startTick, int duration, boolean squashing, int normalX, int normalZ) {
            this.startTick = startTick;
            this.duration = Math.max(duration, 1);
            this.squashing = squashing;
            this.normalX = normalX;
            this.normalZ = normalZ;
        }

        float progress(float partialTick) {
            return MathHelper.clamp((clientTicks + partialTick - startTick) / duration, 0, 1);
        }

        /** 1 = normal, MIN_THICKNESS = flat against the wall */
        public float thickness(float partialTick) {
            float t = progress(partialTick);
            float eased = t * t * (3 - 2 * t);
            return squashing ? MathHelper.lerp(eased, 1F, MIN_THICKNESS) : MathHelper.lerp(eased, MIN_THICKNESS, 1F);
        }
    }

    public static void start(int entityId, int normalX, int normalZ, int duration, boolean squashing) {
        STATES.put(entityId, new State(clientTicks, duration, squashing, normalX, normalZ));
    }

    /** @return the squash state of the entity, or null if it is not being squashed */
    public static State get(int entityId) {
        return STATES.get(entityId);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clientTicks++;
        Iterator<State> it = STATES.values().iterator();
        while (it.hasNext()) {
            State state = it.next();
            int age = clientTicks - state.startTick;
            if (state.squashing ? age > state.duration + SAFETY_TIMEOUT : age > state.duration) {
                it.remove();
            }
        }
    }
}
