package com.leecrafts.cloudrider.capability;

import com.leecrafts.cloudrider.capability.cloudsteedentity.ICloudSteedEntityCap;
import com.leecrafts.cloudrider.capability.cloudsteeditem.ICloudSteedItemCap;
import com.leecrafts.cloudrider.capability.lightning.ILightningCap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    // This is a capability for the vanilla lightning bolt, not this mod's lightning bolt projectile.
    public static final Capability<ILightningCap> LIGHTNING_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ICloudSteedItemCap> CLOUD_STEED_ITEM_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ICloudSteedEntityCap> CLOUD_STEED_ENTITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

}
