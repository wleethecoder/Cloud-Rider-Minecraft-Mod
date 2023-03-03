package com.leecrafts.cloudrider.item;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CloudRider.MODID);

    public static final RegistryObject<Item> CLOUD_RIDER_SPAWN_EGG = ITEMS.register("cloud_rider_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.CLOUD_RIDER, 0xffffff, 0xf3ff00,
                    new Item.Properties()));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
