package com.leecrafts.cloudrider.entity;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import com.leecrafts.cloudrider.entity.custom.LightningBoltProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CloudRider.MODID);

    public static final RegistryObject<EntityType<CloudRiderEntity>> CLOUD_RIDER =
            ENTITY_TYPES.register("cloud_rider",
                    () -> EntityType.Builder.of(CloudRiderEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build(new ResourceLocation(CloudRider.MODID, "cloud_rider").toString()));

    public static final RegistryObject<EntityType<LightningBoltProjectileEntity>> LIGHTNING_BOLT_PROJECTILE =
            ENTITY_TYPES.register("lightning_bolt_projectile",
                    () -> EntityType.Builder.of((EntityType.EntityFactory<LightningBoltProjectileEntity>) LightningBoltProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .build(new ResourceLocation(CloudRider.MODID, "lightning_bolt_projectile").toString()));

    public static final RegistryObject<EntityType<CloudSteedEntity>> CLOUD_STEED =
            ENTITY_TYPES.register("cloud_steed",
                    () -> EntityType.Builder.of(CloudSteedEntity::new, MobCategory.MISC)
                            .sized(1.5f, 0.5625f)
                            .build(new ResourceLocation(CloudRider.MODID, "cloud_steed").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
