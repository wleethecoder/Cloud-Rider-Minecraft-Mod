package com.leecrafts.cloudrider.capability.cloudriderentity;

import com.leecrafts.cloudrider.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudRiderCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final CloudRiderCap cloudRiderCap = new CloudRiderCap();

    private final LazyOptional<ICloudRiderCap> cloudRiderCapLazyOptional = LazyOptional.of(() -> cloudRiderCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.CLOUD_RIDER_CAPABILITY.orEmpty(cap, cloudRiderCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.CLOUD_RIDER_CAPABILITY == null) return nbt;
        nbt.putInt("player_id", cloudRiderCap.playerId);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.CLOUD_RIDER_CAPABILITY != null) {
            cloudRiderCap.playerId = nbt.getInt("player_id");
        }
    }

    public void invalidate() {
        cloudRiderCapLazyOptional.invalidate();
    }

}
