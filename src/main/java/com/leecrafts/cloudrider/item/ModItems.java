package com.leecrafts.cloudrider.item;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import com.leecrafts.cloudrider.item.custom.CloudSteedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CloudRider.MODID);

    public static final RegistryObject<Item> WHITE_CLOUD_RIDER_SPAWN_EGG = ITEMS.register("white_cloud_rider_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.WHITE_CLOUD_RIDER, 0xffffff, 0xffffa1,
                    new Item.Properties()));

    public static final RegistryObject<Item> GRAY_CLOUD_RIDER_SPAWN_EGG = ITEMS.register("gray_cloud_rider_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.GRAY_CLOUD_RIDER, 0x636363, 0xffffa1,
                    new Item.Properties()));

    public static final RegistryObject<Item> WHITE_CLOUD_STEED_ITEM = ITEMS.register("white_cloud_steed",
            () -> new CloudSteedItem(CloudSteedEntity.Type.WHITE, (new Item.Properties()).stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GRAY_CLOUD_STEED_ITEM = ITEMS.register("gray_cloud_steed",
            () -> new CloudSteedItem(CloudSteedEntity.Type.GRAY, (new Item.Properties()).stacksTo(1).rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
