package com.leecrafts.cloudrider.capability.lightning;

import com.leecrafts.cloudrider.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightningCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final LightningCap lightningCap = new LightningCap();

    private final LazyOptional<ILightningCap> lightningCapLazyOptional = LazyOptional.of(() -> lightningCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.LIGHTNING_CAPABILITY.orEmpty(cap, lightningCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.LIGHTNING_CAPABILITY == null) return nbt;
        nbt.putBoolean("from_gray_cloud_steed", lightningCap.fromGrayCloudSteed);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.LIGHTNING_CAPABILITY != null) {
            lightningCap.fromGrayCloudSteed = nbt.getBoolean("from_gray_cloud_steed");
        }
    }

    public void invalidate() {
        lightningCapLazyOptional.invalidate();
    }

}
