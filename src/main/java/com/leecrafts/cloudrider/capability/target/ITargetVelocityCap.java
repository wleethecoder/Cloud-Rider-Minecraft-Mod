package com.leecrafts.cloudrider.capability.target;

import net.minecraft.world.entity.LivingEntity;

public interface ITargetVelocityCap {

    void initialize(LivingEntity livingEntity);
    void updatePreviousPos(LivingEntity livingEntity);
    void updateCurrentPos(LivingEntity livingEntity);
    void updateVelocity();

}
