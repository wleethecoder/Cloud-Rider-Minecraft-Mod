package com.leecrafts.cloudrider.capability.target;

import com.leecrafts.cloudrider.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TargetVelocityCapProvider implements ICapabilitySerializable<CompoundTag> {

    public final TargetVelocityCap targetVelocityCap = new TargetVelocityCap();

    private final LazyOptional<ITargetVelocityCap> targetVelocityCapLazyOptional = LazyOptional.of(() -> targetVelocityCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.TARGET_VELOCITY_CAPABILITY.orEmpty(cap, targetVelocityCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.TARGET_VELOCITY_CAPABILITY == null) return nbt;
        nbt.putDouble("velocity_when_targeted_by_cloud_rider", targetVelocityCap.velocity);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.TARGET_VELOCITY_CAPABILITY != null) {
            targetVelocityCap.velocity = nbt.getDouble("velocity_when_targeted_by_cloud_rider");
        }
    }

    public void invalidate() {
        targetVelocityCapLazyOptional.invalidate();
    }

}
