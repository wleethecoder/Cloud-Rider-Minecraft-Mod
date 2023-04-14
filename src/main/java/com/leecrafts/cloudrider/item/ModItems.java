package com.leecrafts.cloudrider.item;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import com.leecrafts.cloudrider.item.custom.CloudSteedItem;
import com.leecrafts.cloudrider.item.custom.FoggySpongeItem;
import com.leecrafts.cloudrider.item.custom.MistySpongeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CloudRider.MODID);

    public static final RegistryObject<Item> CLOUD_RIDER_SPAWN_EGG = ITEMS.register("cloud_rider_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.CLOUD_RIDER, 0xffffff, 0xffffa1,
                    new Item.Properties()));

    public static final RegistryObject<Item> WHITE_CLOUD_STEED_ITEM = ITEMS.register("white_cloud_steed",
            () -> new CloudSteedItem(CloudSteedEntity.Type.WHITE, (new Item.Properties()).stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));

    public static final RegistryObject<Item> GRAY_CLOUD_STEED_ITEM = ITEMS.register("gray_cloud_steed",
            () -> new CloudSteedItem(CloudSteedEntity.Type.GRAY, (new Item.Properties()).stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));

    public static final RegistryObject<Item> MISTY_SPONGE_ITEM = ITEMS.register("misty_sponge",
            () -> new MistySpongeItem(Blocks.SPONGE, new Item.Properties()));

    public static final RegistryObject<Item> FOGGY_SPONGE_ITEM = ITEMS.register("foggy_sponge",
            () -> new FoggySpongeItem(Blocks.SPONGE, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = CloudRider.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModItemsEvents {

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                DispenserBlock.registerBehavior(ModItems.WHITE_CLOUD_STEED_ITEM.get(), new CloudSteedItem.CloudSteedDispenseItemBehavior(CloudSteedEntity.Type.WHITE));
                DispenserBlock.registerBehavior(ModItems.GRAY_CLOUD_STEED_ITEM.get(), new CloudSteedItem.CloudSteedDispenseItemBehavior(CloudSteedEntity.Type.GRAY));
            });
        }

    }

}
