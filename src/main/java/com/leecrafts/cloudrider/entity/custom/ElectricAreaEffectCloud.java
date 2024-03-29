package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.sound.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ElectricAreaEffectCloud extends AreaEffectCloud {

    // An area effect cloud that deals 3 damage (per 0.5 sec) and slows down movement

    public int ambientSoundTime;

    public ElectricAreaEffectCloud(EntityType<? extends ElectricAreaEffectCloud> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setParticle(ParticleTypes.ELECTRIC_SPARK);
        this.setRadius(2.5f);
        this.setWaiting(false);
        this.setDuration(3 * TICKS_PER_SECOND);
    }

    public ElectricAreaEffectCloud(Level level, double xPos, double yPos, double zPos) {
        this(ModEntityTypes.ELECTRIC_AREA_EFFECT_CLOUD.get(), level);
        this.setPos(xPos, yPos - this.getRadius(), zPos);
        this.playAmbientSound();
    }

    @Override
    public void tick() {
        this.baseTick();
        float radius = this.getRadius();
        if (this.level.isClientSide) {
            int numParticles = Mth.ceil((float) Math.PI * radius * radius);
            for (int i = 0; i < numParticles; i++) {
                ParticleOptions particleOptions = this.getParticle();
                double x = this.getX() - radius + (this.random.nextDouble() * radius * 2);
                double y = this.getY() + (this.random.nextDouble() * radius * 2);
                double z = this.getZ() - radius + (this.random.nextDouble() * radius * 2);
                this.level.addAlwaysVisibleParticle(particleOptions, x, y, z, 0, 0, 0);
            }
        }
        else {
            if (this.tickCount >= this.getDuration()) {
                this.discard();
            }
            else {
                List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                for (LivingEntity livingEntity : list) {
                    if (livingEntity instanceof Player) {
                        livingEntity.hurtMarked = true;
                    }
                    livingEntity.hurt(new DamageSource("electrocution"), 3);
//                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(0.3));
                    // Maximum speed of an electrified entity is 3 m/s
                    Vec3 vec3 = livingEntity.getDeltaMovement();
                    double slowdownSpeed = 3.0;
                    if (livingEntity.isFallFlying()) {
                        slowdownSpeed = 20.0;
                    }
                    livingEntity.setDeltaMovement(vec3.normalize().scale(Math.min(vec3.length(), slowdownSpeed / TICKS_PER_SECOND)));
                }
            }
        }

        if (this.random.nextInt(10) < this.ambientSoundTime) {
            this.playAmbientSound();
        }
        else {
            this.ambientSoundTime++;
        }
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        return EntityDimensions.fixed(this.getRadius() * 2, this.getRadius() * 2);
    }

    private void playAmbientSound() {
        this.playSound(ModSounds.ELECTRIC_AREA_EFFECT_CLOUD_AMBIENT.get(), 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        this.resetAmbientSoundTime();
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -5;
    }

}
