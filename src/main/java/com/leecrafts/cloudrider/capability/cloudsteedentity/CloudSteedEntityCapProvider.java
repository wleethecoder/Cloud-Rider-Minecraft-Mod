package com.leecrafts.cloudrider.capability.cloudsteedentity;

import com.leecrafts.cloudrider.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudSteedEntityCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final CloudSteedEntityCap cloudSteedEntityCap = new CloudSteedEntityCap();

    private final LazyOptional<ICloudSteedEntityCap> cloudSteedEntityCapLazyOptional = LazyOptional.of(() -> cloudSteedEntityCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.CLOUD_STEED_ENTITY_CAPABILITY.orEmpty(cap, cloudSteedEntityCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.CLOUD_STEED_ENTITY_CAPABILITY == null) return nbt;
        nbt.putBoolean("player_passenger_had_logged_out", cloudSteedEntityCap.playerPassengerHadLoggedOut);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.CLOUD_STEED_ENTITY_CAPABILITY != null) {
            cloudSteedEntityCap.playerPassengerHadLoggedOut = nbt.getBoolean("player_passenger_had_logged_out");
        }
    }

    public void invalidate() {
        cloudSteedEntityCapLazyOptional.invalidate();
    }

}
