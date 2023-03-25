package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.capability.ModCapabilities;
import com.leecrafts.cloudrider.capability.cloudriderentity.CloudRiderCap;
import com.leecrafts.cloudrider.capability.player.PlayerCap;
import com.leecrafts.cloudrider.config.CloudRiderCommonConfigs;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class CloudRiderEntity extends FlyingMob implements GeoAnimatable, Enemy {

    public static final int CLOUD_LEVEL = 192;
    public static final int LOWEST_LEVEL_OVERWORLD = 112;
    public static final int TARGET_LEVEL_END = 80;
    public static final int LOWEST_LEVEL_END = 70;
    public static final double MOVEMENT_SPEED_PER_SECOND = 45;
    public static final double SECONDS_PER_ATTACK = 0.25;
    public static final float ATTACK_DAMAGE = 4.0f;
    public static final float PROJECTILE_SPEED_PER_SECOND = 50.0f;
    public static final float LOOK_RANGE = 32.0f;
    public static final float MIN_CHASE_DISTANCE = 16.0f;
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(CloudRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(CloudRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("animation.cloud_rider.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.cloud_rider.attack");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.cloud_rider.charge");
    private static final ResourceLocation LOOT_TABLE_WHITE = new ResourceLocation(CloudRider.MODID, "entities/white_cloud_rider");
    private static final ResourceLocation LOOT_TABLE_GRAY = new ResourceLocation(CloudRider.MODID, "entities/gray_cloud_rider");
    public static final DamageSource VAPORIZE = new DamageSource("vaporize");

    public CloudRiderEntity(EntityType<? extends CloudRiderEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = XP_REWARD_LARGE;
        this.moveControl = new CloudRiderEntity.CloudRiderMoveControl(this);
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.FOLLOW_RANGE, LOOK_RANGE).build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new CloudRiderEntity.ChaseTargetGoal(this));
        this.goalSelector.addGoal(4, new CloudRiderEntity.MoveToCloudLevelGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new CloudRiderEntity.CloudRiderEntityHurtByTargetGoal(this, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 0, true, false, this::canAttackMob));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, this::canAttackPlayer));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            // Dies in the Nether
            if (this.level.dimensionType().ultraWarm()) {
                this.vaporize();
            }

            if (this.tickCount % 20 == 0 &&
                    !this.isDeadOrDying() &&
                    this.getType() == ModEntityTypes.WHITE_CLOUD_RIDER.get() &&
                    this.level.isThundering() &&
                    !this.isPersistenceRequired()) {
                ServerLevel serverLevel = (ServerLevel) this.level;
                CloudRiderEntity grayCloudRiderEntity = ModEntityTypes.GRAY_CLOUD_RIDER.get().create(serverLevel);
                if (grayCloudRiderEntity != null) {
                    grayCloudRiderEntity.setHealth(this.getHealth());
                    // TODO rotation
                    grayCloudRiderEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    grayCloudRiderEntity.setNoAi(this.isNoAi());
                    serverLevel.addFreshEntityWithPassengers(grayCloudRiderEntity);
                    this.playSound(SoundEvents.BEACON_POWER_SELECT);
                    // TODO particles
                    this.discard();
                }
            }

            this.getCapability(ModCapabilities.CLOUD_RIDER_CAPABILITY).ifPresent(iCloudRiderCap -> {
                CloudRiderCap cloudRiderCap = (CloudRiderCap) iCloudRiderCap;
                boolean persistenceChanged = this.isPersistenceRequired() == cloudRiderCap.wasNotPersistent;
                if (persistenceChanged) {
                    if (this.level.getEntity(cloudRiderCap.playerId) instanceof Player player) {
                        player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                            PlayerCap playerCap = (PlayerCap) iPlayerCap;
                            if (this.isPersistenceRequired()) {
                                if (playerCap.numCloudRiders > 0) playerCap.numCloudRiders--;
                                cloudRiderCap.wasNotPersistent = false;
                            }
                            else if (!this.isPersistenceRequired()) {
                                playerCap.numCloudRiders++;
                                cloudRiderCap.wasNotPersistent = true;
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ATTACKING, false);
        this.entityData.define(DATA_IS_CHARGING, false);
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (pSource == DamageSource.LAVA || pSource == DamageSource.LIGHTNING_BOLT) {
            return this.vaporize();
        }
        return super.hurt(pSource, pAmount);
    }

    // TODO particles
    private boolean vaporize() {
        if (this.isDeadOrDying()) return false;
        this.playSound(SoundEvents.FIRE_EXTINGUISH);
        this.playSound(ModSounds.CLOUD_RIDER_VAPORIZE.get());
        return super.hurt(VAPORIZE, Float.MAX_VALUE);
    }

    @Override
    public void remove(@NotNull RemovalReason removalReason) {
        this.getCapability(ModCapabilities.CLOUD_RIDER_CAPABILITY).ifPresent(iCloudRiderCap -> {
            CloudRiderCap cloudRiderCap = (CloudRiderCap) iCloudRiderCap;
            if (this.level.getEntity(cloudRiderCap.playerId) instanceof Player player) {
                player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    if (playerCap.numCloudRiders > 0) playerCap.numCloudRiders--;
                });
            }
        });
        super.remove(removalReason);
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        if (!this.isAttacking()) {
            event.getController().setAnimation(IDLE);
        }
        else if (this.isCharging()) {
            event.getController().setAnimation(CHARGE);
        }
        else {
            event.getController().setAnimation(ATTACK);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "controller", 0, this::predicate)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return ((Entity) o).tickCount;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CLOUD_RIDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    protected @NotNull ResourceLocation getDefaultLootTable() {
        if (this.getType() == ModEntityTypes.WHITE_CLOUD_RIDER.get()) {
            return LOOT_TABLE_WHITE;
        }
        return LOOT_TABLE_GRAY;
    }

    @Override
    public boolean canAttackType(@NotNull EntityType<?> type) {
        return true;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_IS_ATTACKING);
    }

    public void setAttacking(boolean isAttacking) {
        this.entityData.set(DATA_IS_ATTACKING, isAttacking);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean isCharging) {
        this.entityData.set(DATA_IS_CHARGING, isCharging);
    }

    public static boolean isValidSpawn(BlockPos blockPos, ServerLevel serverLevel) {
        if (!serverLevel.getBlockState(blockPos.below()).isAir()) return false;
        for (int i = 0; i < 2; i++) {
            BlockPos blockPos1 = blockPos.above(i);
            BlockState blockState1 = serverLevel.getBlockState(blockPos1);
            if (!NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos, blockState1, blockState1.getFluidState(), ModEntityTypes.WHITE_CLOUD_RIDER.get())) {
                return false;
            }
        }
        return true;
    }

    public static double getTargetLevel(LivingEntity livingEntity) {
        return livingEntity.level.dimension() == Level.OVERWORLD ? CLOUD_LEVEL : TARGET_LEVEL_END;
    }

    public static double getLowestLevel(LivingEntity livingEntity) {
        return livingEntity.level.dimension() == Level.OVERWORLD ? LOWEST_LEVEL_OVERWORLD : LOWEST_LEVEL_END;
    }

    private boolean canAttackMob(LivingEntity mob) {
        return mob instanceof EnderDragon ||
                mob instanceof FlyingAnimal ||
                mob instanceof Chicken ||
                mob instanceof Bat ||
                (mob instanceof FlyingMob && !(mob instanceof CloudRiderEntity));
    }

    private boolean canAttackPlayer(LivingEntity player) {
        return CloudRiderCommonConfigs.CLOUD_RIDER_IS_HOSTILE.get() &&
                player.level.dimension() == Level.OVERWORLD &&
                (player.getVehicle() == null || player.getVehicle().getType() != ModEntityTypes.CLOUD_STEED.get()) &&
                (player.getActiveEffectsMap() == null || !player.hasEffect(MobEffects.INVISIBILITY));
    }

    private void shootTarget(LivingEntity shooter, LivingEntity target, float damage, boolean isCharged) {
        LightningBoltProjectileEntity projectile = new LightningBoltProjectileEntity(
                shooter.level,
                shooter,
                damage,
                isCharged);
        projectile.shoot(target, PROJECTILE_SPEED_PER_SECOND / TICKS_PER_SECOND);
        shooter.level.addFreshEntity(projectile);
    }

    static class CloudRiderMoveControl extends MoveControl {

        private final CloudRiderEntity cloudRiderEntity;

        public CloudRiderMoveControl(CloudRiderEntity cloudRiderEntity) {
            super(cloudRiderEntity);
            this.cloudRiderEntity = cloudRiderEntity;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.getWantedX() - this.cloudRiderEntity.getX(),
                        this.getWantedY() - this.cloudRiderEntity.getY(),
                        this.getWantedZ() - this.cloudRiderEntity.getZ());
                double travelDistance = vec3.length();
                double offsetDistance = 3;
                double slowdownVelocity = (travelDistance - offsetDistance) * 4;
                vec3 = vec3.normalize();
                if (this.cloudRiderEntity.getTarget() != null) {
                    offsetDistance = MIN_CHASE_DISTANCE;
                    slowdownVelocity = (travelDistance - offsetDistance) * 8;
                }
                if (travelDistance > offsetDistance) {
                    this.cloudRiderEntity.setDeltaMovement(vec3.scale(Math.min(MOVEMENT_SPEED_PER_SECOND, slowdownVelocity) / TICKS_PER_SECOND));
                }
                else {
//                    System.out.println("Movement goal already reached");
                    this.operation = Operation.WAIT;
                }
            }
        }

    }

    static class ChaseTargetGoal extends Goal {

        public int cooldown;
        public int chargePhaseTimer;
        public boolean postCharge;
        public int postChargeCooldown;
        public float damage;

        private final CloudRiderEntity cloudRiderEntity;

        public ChaseTargetGoal(CloudRiderEntity cloudRiderEntity) {
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.TARGET));
            this.damage = ATTACK_DAMAGE;
            // gray cloud riders do 33.3% more damage
            if (this.cloudRiderEntity.getType() == ModEntityTypes.GRAY_CLOUD_RIDER.get()) {
                this.damage *= (4.0 / 3);
            }
        }

        @Override
        public boolean canUse() {
            return this.cloudRiderEntity.getTarget() != null;
        }

        @Override
        public void start() {
            this.cooldown = 0;
            this.chargePhaseTimer = 0;
            this.postCharge = false;
            this.postChargeCooldown = 0;
            this.cloudRiderEntity.setAttacking(true);
        }

        @Override
        public void stop() {
            this.cloudRiderEntity.setAttacking(false);
            this.cloudRiderEntity.setCharging(false);
            this.cloudRiderEntity.getMoveControl().setWantedPosition(this.cloudRiderEntity.getX(), this.cloudRiderEntity.getY(), this.cloudRiderEntity.getZ(), 1);
        }

        @Override
        public void tick() {
            LivingEntity target = this.cloudRiderEntity.getTarget();
            if (target != null) {
//                System.out.println("target dist: " + target.distanceTo(this.cloudRiderEntity));
                // Putting this condition may seem redundant but ensures that setWantedPosition()
                // does not get called when unnecessary
                if (target.distanceTo(this.cloudRiderEntity) > MIN_CHASE_DISTANCE) {
                    this.cloudRiderEntity.getMoveControl().setWantedPosition(
                            target.getX(),
                            Math.max(target.getY(), getLowestLevel(this.cloudRiderEntity)),
                            target.getZ(),
                            1
                    );
                }
                if (!this.cloudRiderEntity.isCharging()) {
                    if (this.cooldown++ >= SECONDS_PER_ATTACK * TICKS_PER_SECOND) {
                        this.cloudRiderEntity.shootTarget(this.cloudRiderEntity, target, this.damage, false);
                        this.cloudRiderEntity.playSound(ModSounds.CLOUD_RIDER_SHOOT.get());
                        this.cooldown = 0;
                    }

                    if (this.chargePhaseTimer++ >= 10 * TICKS_PER_SECOND) {
                        this.cloudRiderEntity.setCharging(true);
                        this.chargePhaseTimer = 0;
                        this.cooldown = 0;
                        this.cloudRiderEntity.playSound(ModSounds.CLOUD_RIDER_CHARGE.get(), 10.0f, 1.0f);
                    }
                }
                else {
                    if (!this.postCharge) {
                        if (this.cooldown++ >= 3 * TICKS_PER_SECOND) {
                            this.postCharge = true;
                            this.cloudRiderEntity.shootTarget(this.cloudRiderEntity, target, 3 * 2 * this.damage, true);
                            this.cloudRiderEntity.playSound(ModSounds.CLOUD_RIDER_CHARGED_SHOT.get(), 10.0f, 1.0f);
                            this.cooldown = 0;
                        }
                    }
                    else {
                        if (this.cooldown++ >= 1 * TICKS_PER_SECOND) {
                            this.cloudRiderEntity.setCharging(false);
                            this.postCharge = false;
                            this.cooldown = 0;
                        }
                    }
                }
                this.cloudRiderEntity.getLookControl().setLookAt(target, 30.0f, 30.0f);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

    }

    // If it is not attacking and is at any y-level other than ~192, Cloud Rider will go to y=192.
    // If it is attacking and is lower than y=80, then it will go to y=192.
    static class MoveToCloudLevelGoal extends Goal {

        private final CloudRiderEntity cloudRiderEntity;

        public MoveToCloudLevelGoal(CloudRiderEntity cloudRiderEntity) {
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            double y = this.cloudRiderEntity.getY();
            double targetLevel = getTargetLevel(this.cloudRiderEntity);
            return this.cloudRiderEntity.getTarget() == null && (y > targetLevel + 4 || y < targetLevel - 4);
        }

        @Override
        public void start() {
            double x = this.cloudRiderEntity.getX();
            double z = this.cloudRiderEntity.getZ();
            this.cloudRiderEntity.getMoveControl().setWantedPosition(x, getTargetLevel(this.cloudRiderEntity), z, 1);
//            System.out.println("[" + this.cloudRiderEntity.getMoveControl().getWantedX() + ", " +
//                    this.cloudRiderEntity.getMoveControl().getWantedY() + ", " +
//                    this.cloudRiderEntity.getMoveControl().getWantedZ() + "]");
        }

    }

    // HurtByTargetGoal only applies to PathFinderMobs, so I had to manually make a class for Cloud Riders
    static class CloudRiderEntityHurtByTargetGoal extends TargetGoal {

        private final CloudRiderEntity cloudRiderEntity;
        private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
        private int timestamp;
        public float damage;

        public CloudRiderEntityHurtByTargetGoal(CloudRiderEntity cloudRiderEntity, boolean pMustSee) {
            super(cloudRiderEntity, pMustSee);
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.TARGET));
            this.damage = ATTACK_DAMAGE;
            // gray cloud riders do 33.3% more damage
            if (this.cloudRiderEntity.getType() == ModEntityTypes.GRAY_CLOUD_RIDER.get()) {
                this.damage *= (4.0 / 3);
            }
        }

        @Override
        public boolean canUse() {
            int i = this.mob.getLastHurtByMobTimestamp();
            LivingEntity livingentity = this.mob.getLastHurtByMob();
            if (i != this.timestamp && livingentity != null && !(livingentity instanceof CloudRiderEntity)) {
                if (livingentity.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                    return false;
                } else {
                    // if it cannot see target, it will still retaliate with a single shot
                    if (livingentity.distanceTo(this.cloudRiderEntity) > LOOK_RANGE) {
                        this.cloudRiderEntity.shootTarget(this.cloudRiderEntity, livingentity, this.damage, true);
                        this.cloudRiderEntity.playSound(ModSounds.CLOUD_RIDER_SHOOT.get());
                    }
                    return this.canAttack(livingentity, HURT_BY_TARGETING);
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.cloudRiderEntity.setTarget(this.cloudRiderEntity.getLastHurtByMob());
            this.targetMob = this.cloudRiderEntity.getTarget();
            this.timestamp = this.cloudRiderEntity.getLastHurtByMobTimestamp();
            this.unseenMemoryTicks = 300;
            super.start();
        }

    }

}
