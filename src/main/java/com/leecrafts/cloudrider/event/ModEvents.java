package com.leecrafts.cloudrider.event;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.capability.player.ITargetVelocityCap;
import com.leecrafts.cloudrider.capability.player.TargetVelocityCapProvider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import com.leecrafts.cloudrider.entity.custom.LightningBoltProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = CloudRider.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(ITargetVelocityCap.class);
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventPlayerVelocityCap(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity livingEntity) {
                TargetVelocityCapProvider targetVelocityCapProvider = new TargetVelocityCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "velocity_when_targeted_by_cloud_rider"), targetVelocityCapProvider);
                if (!(livingEntity instanceof Player)) {
                    event.addListener(targetVelocityCapProvider::invalidate);
                }
            }
        }

//        @SubscribeEvent
//        public static void targetTick(LivingEvent.LivingTickEvent event) {
//
//        }

    }

    @Mod.EventBusSubscriber(modid = CloudRider.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.CLOUD_RIDER.get(), CloudRiderEntity.setAttributes());
        }

    }

}
