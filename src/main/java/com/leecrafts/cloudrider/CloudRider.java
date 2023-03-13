package com.leecrafts.cloudrider;

import com.leecrafts.cloudrider.client.cloudrider.CloudRiderRenderer;
import com.leecrafts.cloudrider.client.cloudsteed.CloudSteedRenderer;
import com.leecrafts.cloudrider.client.lightningboltprojectile.LightningBoltProjectileRenderer;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.item.ModItems;
import com.leecrafts.cloudrider.sound.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CloudRider.MODID)
public class CloudRider
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "cloudrider";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public CloudRider()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);

        ModEntityTypes.register(modEventBus);

        GeckoLib.initialize();

//        // Register the commonSetup method for modloading
//        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

//    private void commonSetup(final FMLCommonSetupEvent event)
//    {
//    }

    private void addCreative(CreativeModeTabEvent.BuildContents event)
    {
        if (event.getTab() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.CLOUD_RIDER_SPAWN_EGG);
        }
    }

//    // You can use SubscribeEvent and let the Event Bus discover methods to call
//    @SubscribeEvent
//    public void onServerStarting(ServerStartingEvent event)
//    {
//    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            EntityRenderers.register(ModEntityTypes.CLOUD_RIDER.get(), CloudRiderRenderer::new);
            EntityRenderers.register(ModEntityTypes.LIGHTNING_BOLT_PROJECTILE.get(), LightningBoltProjectileRenderer::new);
            EntityRenderers.register(ModEntityTypes.CLOUD_STEED.get(), CloudSteedRenderer::new);
        }
    }
}
