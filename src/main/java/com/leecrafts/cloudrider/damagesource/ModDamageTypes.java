package com.leecrafts.cloudrider.damagesource;

import com.leecrafts.cloudrider.CloudRider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CloudRider.MODID)
public class ModDamageTypes {

    public static final ResourceKey<DamageType> VAPORIZE = register("vaporize");
    public static final ResourceKey<DamageType> ELECTROCUTION = register("electrocution");

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(CloudRider.MODID, name));
    }

    protected static void bootstrap(BootstapContext<DamageType> context) {
        context.register(VAPORIZE, new DamageType("vaporize", 1000.0f));
        context.register(ELECTROCUTION, new DamageType("electrocution", 0.0f));
    }

    @SubscribeEvent
    public static void gatherDataEvent(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        dataGenerator.addProvider(
                event.includeServer(),
                new DatapackBuiltinEntriesProvider(packOutput, event.getLookupProvider(), BUILDER, Set.of(CloudRider.MODID))
        );
    }

}
