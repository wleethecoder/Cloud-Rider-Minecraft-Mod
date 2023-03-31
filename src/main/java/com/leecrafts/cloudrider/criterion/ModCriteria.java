package com.leecrafts.cloudrider.criterion;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.criterion.custom.EntityTargetEntityTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModCriteria {

    public static EntityTargetEntityTrigger ENTITY_TARGET_ENTITY;

    @Mod.EventBusSubscriber(modid = CloudRider.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModCriteriaEvents {

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                ENTITY_TARGET_ENTITY = CriteriaTriggers.register(new EntityTargetEntityTrigger());
            });
        }

    }

}
