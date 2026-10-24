package com.rotpaddon.bdth.event;

import com.rotpaddon.bdth.entity.GhostBulletEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Posted on the Forge event bus (server side) once for every living entity a ghost bullet passes through.
 * Ghost bullets never hurt the living by themselves; this is the extension point for other Stands and addons
 * (for example Kiss, which can give a ghost bullet a duplicate that does hurt). Set {@link #setHandled()} to
 * suppress the default pass-through visuals.
 */
public class GhostBulletPassThroughEvent extends Event {
    private final GhostBulletEntity bullet;
    private final LivingEntity target;
    private boolean handled;

    public GhostBulletPassThroughEvent(GhostBulletEntity bullet, LivingEntity target) {
        this.bullet = bullet;
        this.target = target;
    }

    public GhostBulletEntity getBullet() {
        return bullet;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled() {
        this.handled = true;
    }
}
