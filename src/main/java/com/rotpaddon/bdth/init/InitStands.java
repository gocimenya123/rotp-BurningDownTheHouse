package com.rotpaddon.bdth.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.NoSummonStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mod.StoryPart;
import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.action.GhostCloseCrackAction;
import com.rotpaddon.bdth.action.GhostEnterRoomAction;
import com.rotpaddon.bdth.action.GhostGrabAction;
import com.rotpaddon.bdth.action.GhostInviteAction;
import com.rotpaddon.bdth.action.GhostOpenCrackAction;
import com.rotpaddon.bdth.action.GhostPocketAction;
import com.rotpaddon.bdth.action.GhostReleaseAction;
import com.rotpaddon.bdth.stand.BdthStandType;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), BdthAddon.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), BdthAddon.MOD_ID);

    // ======================================== Burning Down the House ========================================

    // The crack: opened in a wall (shift: closed again)
    public static final RegistryObject<StandAction> OPEN_CRACK = ACTIONS.register("bdth_open_crack", 
            () -> new GhostOpenCrackAction(new StandAction.Builder().cooldown(40)));

    public static final RegistryObject<StandAction> CLOSE_CRACK = ACTIONS.register("bdth_close_crack", 
            () -> new GhostCloseCrackAction(new StandAction.Builder().shiftVariationOf(OPEN_CRACK)));

    // Through the crack into the ghost room, and back
    public static final RegistryObject<StandAction> ENTER_ROOM = ACTIONS.register("bdth_enter_room", 
            () -> new GhostEnterRoomAction(new StandAction.Builder().cooldown(40)));

    // Whoever is invited can follow the user in
    public static final RegistryObject<StandAction> INVITE = ACTIONS.register("bdth_invite", 
            () -> new GhostInviteAction(new StandAction.Builder().cooldown(10)));

    // Storage for ghost objects of any size
    public static final RegistryObject<StandAction> POCKET = ACTIONS.register("bdth_pocket", 
            () -> new GhostPocketAction(new StandAction.Builder()));

    // Taking a ghost object into the pocket (shift: taking it out into the world)
    public static final RegistryObject<StandAction> GRAB = ACTIONS.register("bdth_grab", 
            () -> new GhostGrabAction(new StandAction.Builder().cooldown(5)));

    public static final RegistryObject<StandAction> RELEASE = ACTIONS.register("bdth_release", 
            () -> new GhostReleaseAction(new StandAction.Builder().cooldown(5).shiftVariationOf(GRAB)));

    public static final RegistryObject<StandType<StandStats>> BURNING_DOWN_THE_HOUSE = STANDS.register("burning_down_the_house", 
            () -> new BdthStandType(new NoSummonStandType.Builder<StandStats>()
                    .color(0xB7C4D6)
                    .storyPartName(StoryPart.STONE_OCEAN.getName())
                    .leftClickHotbar(OPEN_CRACK.get())
                    .rightClickHotbar(POCKET.get(), INVITE.get())
                    .defaultStats(StandStats.class, new StandStats.Builder()
                            .power(0)
                            .speed(0)
                            .range(0)
                            .durability(0)
                            .precision(0))));
}
