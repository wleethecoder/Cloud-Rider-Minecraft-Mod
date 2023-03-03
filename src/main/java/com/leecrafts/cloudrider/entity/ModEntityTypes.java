package com.leecrafts.cloudrider.entity;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.resources.ResourceLocation;
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

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
