package com.leecrafts.cloudrider.capability;

import com.leecrafts.cloudrider.capability.player.ITargetVelocityCap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static final Capability<ITargetVelocityCap> TARGET_VELOCITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

}
