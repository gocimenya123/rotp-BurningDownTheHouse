package com.rotpaddon.bdth.event;

import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.capability.GhostUserData;
import com.rotpaddon.bdth.capability.NbtCapabilityProvider;
import com.rotpaddon.bdth.entity.GhostObjectEntity;
import com.rotpaddon.bdth.ghost.room.GhostRoomWorldData;
import com.rotpaddon.bdth.init.InitDimensions;
import com.rotpaddon.bdth.item.GhostItem;
import com.rotpaddon.bdth.network.BdthNetwork;
import com.rotpaddon.bdth.network.packets.GhostDataSyncPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge bus events: attaching the capabilities, keeping the player data alive and in sync, and ghost items thrown out of the inventory. */
@Mod.EventBusSubscriber(modid = BdthAddon.MOD_ID)
public class BdthEventHandler {
    private static final ResourceLocation USER_DATA_CAP = new ResourceLocation(BdthAddon.MOD_ID, "ghost_user");
    private static final ResourceLocation ROOM_DATA_CAP = new ResourceLocation(BdthAddon.MOD_ID, "ghost_rooms");

    @SubscribeEvent
    public static void onAttachEntityCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(USER_DATA_CAP, new NbtCapabilityProvider<>(() -> GhostUserData.CAPABILITY, GhostUserData::new));
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCaps(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (!world.isClientSide() && world.dimension() == InitDimensions.GHOST_ROOM) {
            event.addCapability(ROOM_DATA_CAP, new NbtCapabilityProvider<>(() -> GhostRoomWorldData.CAPABILITY, GhostRoomWorldData::new));
        }
    }

    /** the data survives death and returning from the End */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(GhostUserData.CAPABILITY).ifPresent(old -> 
                event.getPlayer().getCapability(GhostUserData.CAPABILITY).ifPresent(data -> data.copyFrom(old)));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncNow(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncNow(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncNow(event.getPlayer());
    }

    /** whenever something changed (the pocket, the invitations, the crack), the owner's client is told */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayerEntity) {
            GhostUserData.get(event.player).ifPresent(data -> {
                if (data.consumeSyncNeeded()) {
                    BdthNetwork.sendToClient(new GhostDataSyncPacket(data.toSyncNBT()), (ServerPlayerEntity) event.player);
                }
            });
        }
    }

    private static void syncNow(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            GhostUserData.get(player).ifPresent(data -> data.markForSync());
        }
    }

    /**
     * A ghost item cannot lie on the ground like a normal item: dropping it puts the ghost object into the world instead,
     * where it can be seen and picked up by whoever can see ghosts.
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity dropped = event.getEntityItem();
        if (dropped.getItem().getItem() instanceof GhostItem && !dropped.level.isClientSide()) {
            // cancelling the toss means the ItemEntity is never added to the world (the stack has already left the inventory)
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            Vector3d look = player.getLookAngle();
            Vector3d pos = player.position().add(new Vector3d(look.x, 0, look.z).normalize().scale(1.0));
            GhostObjectEntity.spawn(player.level, dropped.getItem(), pos, player.yRot + 180);
        }
    }
}
