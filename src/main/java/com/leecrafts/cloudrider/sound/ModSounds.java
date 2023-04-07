package com.leecrafts.cloudrider.sound;

import com.leecrafts.cloudrider.CloudRider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CloudRider.MODID);

    public static final RegistryObject<SoundEvent> CLOUD_RIDER_AMBIENT = registerSoundEvent("entity.cloud_rider.ambient");
    public static final RegistryObject<SoundEvent> CLOUD_RIDER_HURT = registerSoundEvent("entity.cloud_rider.hurt");
    public static final RegistryObject<SoundEvent> CLOUD_RIDER_DEATH = registerSoundEvent("entity.cloud_rider.death");
    public static final RegistryObject<SoundEvent> CLOUD_RIDER_VAPORIZE = registerSoundEvent("entity.cloud_rider.vaporize");
    public static final RegistryObject<SoundEvent> CLOUD_RIDER_SHOOT = registerSoundEvent("entity.cloud_rider.shoot");
    public static final RegistryObject<SoundEvent> CLOUD_RIDER_CHARGE = registerSoundEvent("entity.cloud_rider.charge");
    public static final RegistryObject<SoundEvent> CLOUD_RIDER_CHARGED_SHOT = registerSoundEvent("entity.cloud_rider.charged_shot");
    public static final RegistryObject<SoundEvent> WHITE_CONVERTED_TO_GRAY = registerSoundEvent("entity.cloud_rider.converted_to_gray");
    public static final RegistryObject<SoundEvent> SPONGE_FILL_CLOUD_RIDER = registerSoundEvent("item.sponge.fill_cloud_rider");
    public static final RegistryObject<SoundEvent> LIGHTNING_BOLT_PROJECTILE_AMBIENT = registerSoundEvent("entity.lightning_bolt_projectile.ambient");
    public static final RegistryObject<SoundEvent> ELECTRIC_AREA_EFFECT_CLOUD_AMBIENT = registerSoundEvent("entity.electric_area_effect_cloud.ambient");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CloudRider.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
