package com.leecrafts.cloudrider.capability.cloudsteeditem;

import com.leecrafts.cloudrider.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudSteedItemCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final CloudSteedItemCap cloudSteedItemCap = new CloudSteedItemCap();

    private final LazyOptional<ICloudSteedItemCap> cloudSteedItemCapLazyOptional = LazyOptional.of(() -> cloudSteedItemCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.CLOUD_STEED_ITEM_CAPABILITY.orEmpty(cap, cloudSteedItemCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.CLOUD_STEED_ITEM_CAPABILITY == null) return nbt;
        nbt.putBoolean("dropped_from_player", cloudSteedItemCap.droppedFromPlayer);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.CLOUD_STEED_ITEM_CAPABILITY != null) {
            cloudSteedItemCap.droppedFromPlayer = nbt.getBoolean("dropped_from_player");
        }
    }

    public void invalidate() {
        cloudSteedItemCapLazyOptional.invalidate();
    }

}
