package com.leecrafts.cloudrider.capability.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class TargetVelocityCap implements ITargetVelocityCap {

    public boolean initialized;
    public double x0;
    public double y0;
    public double z0;
    public double x;
    public double y;
    public double z;
    public double velocity;
    public Vec3 vec3;

    public TargetVelocityCap() {
        this.initialized = false;
        this.x0 = 0;
        this.y0 = 0;
        this.z0 = 0;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.velocity = 0;
        this.vec3 = Vec3.ZERO;
    }

    public void initialize(LivingEntity livingEntity) {
        updatePreviousPos(livingEntity);
        updateCurrentPos(livingEntity);
        this.initialized = true;
    }

    public void updatePreviousPos(LivingEntity livingEntity) {
        this.x0 = livingEntity.getX();
        this.y0 = livingEntity.getY();
        this.z0 = livingEntity.getZ();
    }

    public void updateCurrentPos(LivingEntity livingEntity) {
        this.x = livingEntity.getX();
        this.y = livingEntity.getY();
        this.z = livingEntity.getZ();
    }

    public void updateVelocity() {
        double xDist = this.x - this.x0;
        double yDist = this.y - this.y0;
        double zDist = this.z - this.z0;
        this.vec3 = new Vec3(xDist, yDist, zDist);
        this.velocity = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist) * TICKS_PER_SECOND;
    }

    public void printPos() {
        System.out.println("Previous position: [" + this.x0 + ", " + this.y0 + ", " + this.z0 + "]");
        System.out.println("Current position: [" + this.x + ", " + this.y + ", " + this.z + "]");
    }

}
