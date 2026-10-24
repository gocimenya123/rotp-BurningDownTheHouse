package com.rotpaddon.bdth.init;

import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.entity.GhostBulletEntity;
import com.rotpaddon.bdth.entity.GhostCrackEntity;
import com.rotpaddon.bdth.entity.GhostObjectEntity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, BdthAddon.MOD_ID);

    public static final RegistryObject<EntityType<GhostObjectEntity>> GHOST_OBJECT = ENTITIES.register("ghost_object", 
            () -> EntityType.Builder.<GhostObjectEntity>of(GhostObjectEntity::new, EntityClassification.MISC)
            .sized(1.0F, 1.0F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(20)
            .build(BdthAddon.MOD_ID + ":ghost_object"));

    public static final RegistryObject<EntityType<GhostCrackEntity>> GHOST_CRACK = ENTITIES.register("ghost_crack", 
            () -> EntityType.Builder.<GhostCrackEntity>of(GhostCrackEntity::new, EntityClassification.MISC)
            .sized(0.8F, 2.0F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(20)
            .build(BdthAddon.MOD_ID + ":ghost_crack"));

    public static final RegistryObject<EntityType<GhostBulletEntity>> GHOST_BULLET = ENTITIES.register("ghost_bullet", 
            () -> EntityType.Builder.<GhostBulletEntity>of(GhostBulletEntity::new, EntityClassification.MISC)
            .sized(0.2F, 0.2F).noSummon().clientTrackingRange(8).updateInterval(5)
            .build(BdthAddon.MOD_ID + ":ghost_bullet"));
}
