package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class LightningBoltProjectileEntity extends Projectile implements GeoAnimatable {

    private float damage;
    private double shooterX;
    private double shooterY;
    private double shooterZ;
    private int life;
    private final int LIFE_SPAN = 5;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LightningBoltProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public LightningBoltProjectileEntity(Level level, LivingEntity shooter, float damage) {
        super(ModEntityTypes.LIGHTNING_BOLT_PROJECTILE.get(), level);
        this.setOwner(shooter);
        Vec3 vec3 = shooter.getViewVector(1);
        this.shooterX = shooter.getX() + vec3.x;
        this.shooterY = shooter.getY(0.5);
        this.shooterZ = shooter.getZ() + vec3.z;
        this.setPos(this.shooterX, this.shooterY, this.shooterZ);
        this.damage = damage;
    }

    public void shoot(LivingEntity target, float velocity) {
        double xDir = target.getX() - this.shooterX;
        double yDir = target.getY() - this.shooterY;
        double zDir = target.getZ() - this.shooterZ;
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0f, 1.0f);
        this.shoot(xDir, yDir, zDir, velocity, 0.0f);
    }

    public void tick() {
        super.tick();
        if (this.life >= LIFE_SPAN * TICKS_PER_SECOND) {
            this.discard();
        }
        this.life++;
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitResult)) {
            this.onHit(hitResult);
        }
        double newX = this.getX() + vec3.x;
        double newY = this.getY() + vec3.y;
        double newZ = this.getZ() + vec3.z;
        this.setDeltaMovement(vec3);
        this.updateRotation();
        this.setPos(newX, newY, newZ);
    }

//    public LightningBoltProjectileEntity(EntityType<? extends LightningBoltProjectileEntity> pEntityType, Level pLevel) {
//        super(pEntityType, pLevel);
//    }
//
//
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = this.getOwner();
        if (!this.level.isClientSide() && entity instanceof LivingEntity) {
            result.getEntity().hurt(DamageSource.indirectMobAttack(this, (LivingEntity) entity).setProjectile(), this.damage);
        }
    }

    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide()) {
            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0f, 1.0f);
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }

}