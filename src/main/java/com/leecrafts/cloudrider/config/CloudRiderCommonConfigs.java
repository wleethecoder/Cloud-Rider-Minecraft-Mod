package com.leecrafts.cloudrider.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CloudRiderCommonConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> CLOUD_RIDER_SPAWN_CAP;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CLOUD_RIDER_IS_HOSTILE;

    static {
        BUILDER.push("Configs for Cloud Rider");

        CLOUD_RIDER_SPAWN_CAP = BUILDER.comment("Maximum amount of cloud riders that can spawn per player.")
                .defineInRange("Cloud Rider Spawn Cap", 3, 0, 10);

        CLOUD_RIDER_IS_HOSTILE = BUILDER.comment("Whether or not cloud riders are hostile towards players. If value is set to false, then cloud riders are neutral.")
                .define("Cloud Rider Is Hostile", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
