package com.leecrafts.cloudrider.capability;

import com.leecrafts.cloudrider.capability.cloudriderentity.ICloudRiderCap;
import com.leecrafts.cloudrider.capability.lightning.ILightningCap;
import com.leecrafts.cloudrider.capability.player.IPlayerCap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static final Capability<IPlayerCap> PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ICloudRiderCap> CLOUD_RIDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    // This is a capability of the vanilla lightning bolt, not this mod's lightning bolt projectile.
    public static final Capability<ILightningCap> LIGHTNING_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

}
