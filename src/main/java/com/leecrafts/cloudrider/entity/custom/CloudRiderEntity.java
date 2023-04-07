package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.config.CloudRiderCommonConfigs;
import com.leecrafts.cloudrider.criterion.ModCriteria;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.item.ModItems;
import com.leecrafts.cloudrider.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class CloudRiderEntity extends FlyingMob implements GeoAnimatable, Enemy, VariantHolder<CloudRiderEntity.Type> {

    // The cloud rider is a mob that loves mischief and violence. It'll pick fights with players and anything that flies or has wings.
    // This mob is meant to challenge players late game, so don't try to fight it with iron armor!
    // Make sure you have fully enchanted diamond/netherite armor and bow. And of course an elytra. Golden apples may come in handy as well.

    // Cloud riders attack by rapidly hurling lightning bolts. Keep in mind that certain substances in Minecraft can conduct electricity.
    // Cloud riders can travel as fast as 45 m/s, so think twice before flying away when they are attacking you.

    // Their drops, the cloud steed, are rewarding. If a player rides a cloud steed, cloud riders become neutral.

    // The player can mop up cloud riders with a sponge--if they are not attacking the player.
    // Where would you want to take them? What places (dimensions, biomes, caves, etc.) have mobs that cloud riders would be eager to challenge?

    public static final int CLOUD_LEVEL = 192;
    public static final int LOWEST_LEVEL_OVERWORLD = 112;
    public static final int TARGET_LEVEL_END = 80;
    public static final int LOWEST_LEVEL_END = 70;
    public static final double MOVEMENT_SPEED_PER_SECOND = 45;
    public static final double SECONDS_PER_ATTACK = 0.25;
    public static final float PROJECTILE_SPEED_PER_SECOND = 50.0f;
    public static final float MIN_CHASE_DISTANCE = 16.0f;
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(CloudRiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(CloudRiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(CloudRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGE_COOLDOWN = SynchedEntityData.defineId(CloudRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final AttributeModifier ATTACK_MODIFIER_THUNDER = new AttributeModifier(UUID.fromString("2f27f794-df6a-4439-ad25-adb985db6109"), "Thunder attack boost", 1.0 / 3, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier FOLLOW_RANGE_MODIFIER_DRAGON = new AttributeModifier(UUID.fromString("be080bf5-066a-4789-84d7-1fb9e690e798"), "Ender Dragon follow range increase", 32, AttributeModifier.Operation.ADDITION);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("animation.cloud_rider.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.cloud_rider.attack");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.cloud_rider.charge");
    private static final ResourceLocation LOOT_TABLE_WHITE = new ResourceLocation(CloudRider.MODID, "entities/white_cloud_rider");
    private static final ResourceLocation LOOT_TABLE_GRAY = new ResourceLocation(CloudRider.MODID, "entities/gray_cloud_rider");
    public static final DamageSource VAPORIZE = new DamageSource("vaporize");
    public int chargeAnimationTick;

    public CloudRiderEntity(EntityType<? extends CloudRiderEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = XP_REWARD_LARGE;
        this.moveControl = new CloudRiderEntity.CloudRiderMoveControl(this);
        this.chargeAnimationTick = 0;
    }

    // 30 health, 32 follow range
    // 3 attack damage on easy difficulty, 4 on normal, and 6 on hard
    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ATTACK_DAMAGE, 4).build();
    }

    // If they are not attacking, they will go to the y-level where there are clouds.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new CloudRiderEntity.ChaseTargetGoal(this));
        this.goalSelector.addGoal(4, new CloudRiderEntity.MoveToCloudLevelGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new CloudRiderEntity.CloudRiderEntityHurtByTargetGoal(this, true));
        this.targetSelector.addGoal(1, new CloudRiderEntity.CloudRiderNearestAttackableTargetGoal<>(this, Mob.class, this::canAttackMob));
        this.targetSelector.addGoal(2, new CloudRiderEntity.CloudRiderNearestAttackableTargetGoal<>(this, Player.class, this::canAttackPlayer));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            // They are instantly vaporized in the Nether
            if (this.level.dimensionType().ultraWarm()) {
                this.vaporize();
            }

            // Cloud riders turn gray during a thunderstorm
            // Gray variants are 33% stronger
            // White variants drop white cloud steeds, and gray variants drop gray cloud steeds
            // Gray cloud steeds strike lightning upon enemies on the ground below them
            if (this.tickCount % TICKS_PER_SECOND == 0 &&
                    !this.isDeadOrDying() &&
                    this.getVariant() == Type.WHITE &&
                    this.level.isThundering() &&
                    !this.isPersistenceRequired()) {
                this.setVariant(Type.GRAY);
                this.playSound(ModSounds.WHITE_CONVERTED_TO_GRAY.get());
            }

            AttributeInstance attackDamageAttribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageAttribute != null) {
                if (this.getVariant() == Type.GRAY && !attackDamageAttribute.hasModifier(ATTACK_MODIFIER_THUNDER)) {
                    attackDamageAttribute.addTransientModifier(ATTACK_MODIFIER_THUNDER);
                }
                else if (attackDamageAttribute.hasModifier(ATTACK_MODIFIER_THUNDER)) {
                    attackDamageAttribute.removeModifier(ATTACK_MODIFIER_THUNDER);
                }
            }

            // Follow range increases when the Ender Dragon is around
            AttributeInstance followRangeAttribute = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (this.tickCount % (3 * TICKS_PER_SECOND) == 0 && followRangeAttribute != null) {
                if (this.level.dimension() == Level.END) {
                    List<EnderDragon> enderDragons = this.level.getEntitiesOfClass(EnderDragon.class, this.getBoundingBox().inflate(128));
                    if (enderDragons.size() > 0 && !followRangeAttribute.hasModifier(FOLLOW_RANGE_MODIFIER_DRAGON)) {
                        followRangeAttribute.addTransientModifier(FOLLOW_RANGE_MODIFIER_DRAGON);
                    }
                    else if (followRangeAttribute.hasModifier(FOLLOW_RANGE_MODIFIER_DRAGON)) {
                        followRangeAttribute.removeModifier(FOLLOW_RANGE_MODIFIER_DRAGON);
                    }
                }
                else if (followRangeAttribute.hasModifier(FOLLOW_RANGE_MODIFIER_DRAGON)) {
                    followRangeAttribute.removeModifier(FOLLOW_RANGE_MODIFIER_DRAGON);
                }
            }

            // Challenge advancement is triggered when cloud riders target an ender dragon
            if (this.getTarget() instanceof EnderDragon) {
                if (ModCriteria.ENTITY_TARGET_ENTITY != null) {
                    for (ServerPlayer serverPlayer : this.level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(50))) {
                        ModCriteria.ENTITY_TARGET_ENTITY.trigger(serverPlayer, this.getTarget());
                    }
                }
            }
        }
        else {
            // Particles during charge attack animation
            if (this.isCharging() && this.chargeAnimationTick < 60) {
                Vec3 vec3 = this.getDeltaMovement();
                Vec3 viewVector = this.getViewVector(1);
                double radius = 1 - this.chargeAnimationTick / 60.0;
                for (int i = 0; i < 20; i++) {
                    Vec3 randomVector = new Vec3(1, 1, 1);
                    randomVector = randomVector.xRot(this.random.nextFloat() * 2 * Mth.PI)
                            .yRot(this.random.nextFloat() * 2 * Mth.PI)
                            .zRot(this.random.nextFloat() * 2 * Mth.PI);
                    Vec3 randomVectorSpark = randomVector.normalize().scale(radius);
                    this.level.addAlwaysVisibleParticle(
                            ParticleTypes.ELECTRIC_SPARK,
                            this.getX() + viewVector.x * 0.6 + randomVectorSpark.x,
                            this.getY(0.5) + randomVectorSpark.y,
                            this.getZ() + viewVector.z * 0.6 + randomVectorSpark.z,
                            vec3.x, vec3.y, vec3.z
                    );
                    if (i == 0) {
                        this.level.addAlwaysVisibleParticle(
                                ParticleTypes.END_ROD,
                                this.getX() + viewVector.x * 0.6 + randomVector.x,
                                this.getY(0.5) + randomVector.y,
                                this.getZ() + viewVector.z * 0.6 + randomVector.z,
                                vec3.x - randomVector.x * 0.075,
                                vec3.y - randomVector.y * 0.075,
                                vec3.z - randomVector.z * 0.075
                        );
                    }
                }
                this.chargeAnimationTick++;
            }
            else {
                this.chargeAnimationTick = 0;
            }

            if (this.getVariant() == Type.GRAY) {
                grayCloudParticles(this);
            }
        }
    }

    public static void grayCloudParticles(Entity entity) {
        if (entity.level.random.nextInt(3) == 0) {
            entity.level.addAlwaysVisibleParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    entity.getX() + (entity.level.random.nextFloat() * 0.35 + 0.25) * (entity.level.random.nextBoolean() ? 1 : -1),
                    entity.getY() + entity.level.random.nextFloat() * 0.875 - 0.4375,
                    entity.getZ() + (entity.level.random.nextFloat() * 0.35 + 0.25) * (entity.level.random.nextBoolean() ? 1 : -1),
                    0, 0, 0
            );
        }
    }

    // A sponge becomes a misty sponge when picking up white cloud riders, and a foggy sponge when picking up gray cloud riders
    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack emptySpongeItemStack = pPlayer.getItemInHand(pHand);
        if (emptySpongeItemStack.getItem() == Items.SPONGE && this.isAlive() && this.getTargetId() != pPlayer.getId()) {
            pPlayer.awardStat(Stats.ITEM_USED.get(emptySpongeItemStack.getItem()));
            this.playSound(ModSounds.SPONGE_FILL_CLOUD_RIDER.get());
            ItemStack mistySpongeItemStack = this.getSpongeItemStack();
            if (this.hasCustomName()) {
                mistySpongeItemStack.setHoverName(this.getCustomName());
            }
            ItemStack resultingItemStack = ItemUtils.createFilledResult(emptySpongeItemStack, pPlayer, mistySpongeItemStack);
            pPlayer.setItemInHand(pHand, resultingItemStack);
            this.discard();
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    private ItemStack getSpongeItemStack() {
        return new ItemStack(this.getVariant() == Type.WHITE ?
                ModItems.MISTY_SPONGE_ITEM.get() :
                ModItems.FOGGY_SPONGE_ITEM.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE, 0);
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_IS_CHARGING, false);
        this.entityData.define(DATA_IS_CHARGE_COOLDOWN, false);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setVariant(CloudRiderEntity.Type.byName(pCompound.getString("Type")));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString("Type", this.getVariant().getSerializedName());
    }

    // Cloud riders are not only instantly vaporized by the Nether, but also by lava and lightning bolts (not the
    // lightning bolt projectiles)
    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (!this.level.isClientSide && (pSource == DamageSource.LAVA || pSource == DamageSource.LIGHTNING_BOLT)) {
            return this.vaporize();
        }
        return super.hurt(pSource, pAmount);
    }

    private boolean vaporize() {
        if (this.isDeadOrDying()) return false;
        vaporizeParticles(this);
        this.playSound(SoundEvents.FIRE_EXTINGUISH);
        this.playSound(ModSounds.CLOUD_RIDER_VAPORIZE.get());
        return super.hurt(VAPORIZE, Float.MAX_VALUE);
    }

    public static void vaporizeParticles(Entity entity) {
        for (int i = 0; i < 8; i++) {
            ((ServerLevel) entity.level).sendParticles(
                    ParticleTypes.CLOUD,
                    entity.getX() + entity.level.random.nextDouble(),
                    entity.getY() + 1.2D,
                    entity.getZ() + entity.level.random.nextDouble(),
                    1, 0, 0, 0, 0
            );
        }
    }

    // Experience orbs float
    @Override
    protected void dropExperience() {
        if (this.level instanceof ServerLevel && !this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
            int reward = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.lastHurtByPlayer, this.getExperienceReward());
            while (reward > 0) {
                int i = ExperienceOrb.getExperienceValue(reward);
                reward -= i;
                ExperienceOrb experienceOrb = new ExperienceOrb(this.level, this.getX() + this.random.nextDouble() - 0.5, this.getY(), this.getZ() + this.random.nextDouble() - 0.5, i);
                experienceOrb.setNoGravity(true);
                experienceOrb.setDeltaMovement(Vec3.ZERO);
                this.level.addFreshEntity(experienceOrb);
            }
        }
    }

    // Animation logic
    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        if (this.getTargetId() == -1) {
            event.getController().setAnimation(IDLE);
        }
        else if (this.isCharging() || this.isChargeCooldown()) {
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
        return ModSounds.CLOUD_RIDER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CLOUD_RIDER_DEATH.get();
    }

    // White cloud riders drop 1 white cloud steed when killed by the player
    // Gray cloud riders drop 1 gray cloud steed when killed by the player
    @Override
    protected @NotNull ResourceLocation getDefaultLootTable() {
        if (this.getVariant() == Type.WHITE) {
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

    public int getTargetId() {
        return this.entityData.get(DATA_TARGET_ID);
    }

    public void setTargetId(int targetId) {
        this.entityData.set(DATA_TARGET_ID, targetId);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean isCharging) {
        this.entityData.set(DATA_IS_CHARGING, isCharging);
    }

    public boolean isChargeCooldown() {
        return this.entityData.get(DATA_IS_CHARGE_COOLDOWN);
    }

    public void setChargeCooldown(boolean isChargeCooldown) {
        this.entityData.set(DATA_IS_CHARGE_COOLDOWN, isChargeCooldown);
    }

    // Spawns in the overworld and in the air at y-level 192 (see spawning mechanics at event/ModEvents.java)
    public static boolean isValidSpawn(BlockPos blockPos, ServerLevel serverLevel) {
        if (!serverLevel.getBlockState(blockPos.below()).isAir()) return false;
        for (int i = 0; i < 2; i++) {
            BlockPos blockPos1 = blockPos.above(i);
            BlockState blockState1 = serverLevel.getBlockState(blockPos1);
            if (!NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos, blockState1, blockState1.getFluidState(), ModEntityTypes.CLOUD_RIDER.get())) {
                return false;
            }
        }
        return true;
    }

    public static double getTargetLevel(Level level) {
        return level.dimension() == Level.OVERWORLD ? CLOUD_LEVEL : TARGET_LEVEL_END;
    }

    public static double getLowestLevel(Level level) {
        return level.dimension() == Level.OVERWORLD ? LOWEST_LEVEL_OVERWORLD : LOWEST_LEVEL_END;
    }

    // It's belligerent towards flying things
    private boolean canAttackMob(LivingEntity mob) {
        return mob instanceof EnderDragon ||
                mob instanceof FlyingAnimal ||
                mob instanceof Chicken ||
                mob instanceof Bat ||
                (mob instanceof FlyingMob && !(mob instanceof CloudRiderEntity));
    }

    // Hostile towards players if they are in the overworld and if they are not riding cloud steeds
    // I initially wanted them to not be able to detect invisible players, but that would make them too easy
    private boolean canAttackPlayer(LivingEntity player) {
        return CloudRiderCommonConfigs.CLOUD_RIDER_IS_HOSTILE.get() &&
                player.level.dimension() == Level.OVERWORLD &&
                (player.getVehicle() == null || player.getVehicle().getType() != ModEntityTypes.CLOUD_STEED.get());
//                (player.getActiveEffectsMap() == null || !player.hasEffect(MobEffects.INVISIBILITY));
    }

    private void shootTarget(LivingEntity shooter, LivingEntity target, float damage, boolean isCharged) {
        LightningBoltProjectileEntity projectile = new LightningBoltProjectileEntity(
                shooter.level,
                shooter,
                damage,
                isCharged);
        projectile.shoot(target, PROJECTILE_SPEED_PER_SECOND / TICKS_PER_SECOND);
        shooter.level.addFreshEntity(projectile);
        SoundEvent soundEvent = ModSounds.CLOUD_RIDER_SHOOT.get();
        float volume = 1.0f;
        if (isCharged) {
            soundEvent = ModSounds.CLOUD_RIDER_CHARGED_SHOT.get();
            volume = 10.0f;
        }
        shooter.playSound(soundEvent, volume, 1.0f);
    }

    @Override
    public void setVariant(@NotNull Type pVariant) {
        this.entityData.set(DATA_ID_TYPE, pVariant.getId());
    }

    @Override
    public @NotNull Type getVariant() {
        return CloudRiderEntity.Type.byId(this.entityData.get(DATA_ID_TYPE));
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
                Vec3 vec3 = new Vec3(
                        this.getWantedX() - this.cloudRiderEntity.getX(),
                        this.getWantedY() - this.cloudRiderEntity.getY(),
                        this.getWantedZ() - this.cloudRiderEntity.getZ()
                );
                double travelDistance = vec3.length();
                double offsetDistance = 3;
                double slowdownVelocity = (travelDistance - offsetDistance) * 4;
                vec3 = vec3.normalize();
                // Cloud riders get as close as 16 blocks to their targets
                // They slow down (a little) when they get closer so that their movement won't look so choppy
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

        private final CloudRiderEntity cloudRiderEntity;

        public ChaseTargetGoal(CloudRiderEntity cloudRiderEntity) {
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.cloudRiderEntity.getTarget() != null;
        }

        @Override
        public void start() {
            this.cooldown = 0;
            this.chargePhaseTimer = 0;
            if (this.cloudRiderEntity.getTarget() != null) {
                this.cloudRiderEntity.setTargetId(this.cloudRiderEntity.getTarget().getId());
            }
        }

        @Override
        public void stop() {
            this.cloudRiderEntity.setTargetId(-1);
            this.cloudRiderEntity.setCharging(false);
            this.cloudRiderEntity.setChargeCooldown(false);

            // Resets movement goal
            this.cloudRiderEntity.getMoveControl().setWantedPosition(this.cloudRiderEntity.getX(), this.cloudRiderEntity.getY(), this.cloudRiderEntity.getZ(), 1);
        }

        // Attack logic. Cloud riders attack every 0.25 seconds.
        // After 7 seconds, they start preparing a charged shot, which takes 3 seconds to release.
        // A charged shot deals massive damage and is almost guaranteed to hit.
        @Override
        public void tick() {
            LivingEntity target = this.cloudRiderEntity.getTarget();
            if (target != null) {
                // Putting this condition may seem redundant but ensures that setWantedPosition()
                // does not get called when unnecessary
                if (target.distanceTo(this.cloudRiderEntity) > MIN_CHASE_DISTANCE) {
                    this.cloudRiderEntity.getMoveControl().setWantedPosition(
                            target.getX(),
                            Math.max(target.getY(), getLowestLevel(this.cloudRiderEntity.level)),
                            target.getZ(),
                            1
                    );
                }
                float damage = (float) this.cloudRiderEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (!this.cloudRiderEntity.isCharging() && !this.cloudRiderEntity.isChargeCooldown()) {
                    if (this.cooldown++ >= SECONDS_PER_ATTACK * TICKS_PER_SECOND) {
                        this.cloudRiderEntity.shootTarget(this.cloudRiderEntity, target, damage, false);
                        this.cooldown = 0;
                    }

                    if (this.chargePhaseTimer++ >= 7 * TICKS_PER_SECOND) {
                        this.cloudRiderEntity.setCharging(true);
                        this.chargePhaseTimer = 0;
                        this.cooldown = 0;
                        this.cloudRiderEntity.playSound(ModSounds.CLOUD_RIDER_CHARGE.get(), 10.0f, 1.0f);
                    }
                }
                else {
                    if (!this.cloudRiderEntity.isChargeCooldown()) {
                        if (this.cooldown++ >= 3 * TICKS_PER_SECOND) {
                            this.cloudRiderEntity.setCharging(false);
                            this.cloudRiderEntity.setChargeCooldown(true);
                            // TODO test game balance
                            this.cloudRiderEntity.shootTarget(this.cloudRiderEntity, target, 4.5f * damage, true);
                            this.cooldown = 0;
                        }
                    }
                    else {
                        if (this.cooldown++ >= TICKS_PER_SECOND) {
                            this.cloudRiderEntity.setChargeCooldown(false);
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

    // If it is not attacking and is at any y-level other than ~192, the cloud rider will go to y=192.
    // If it is attacking, it will not go any lower than y=112 (in the overworld).
    static class MoveToCloudLevelGoal extends Goal {

        private final CloudRiderEntity cloudRiderEntity;

        public MoveToCloudLevelGoal(CloudRiderEntity cloudRiderEntity) {
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            double y = this.cloudRiderEntity.getY();
            double targetLevel = getTargetLevel(this.cloudRiderEntity.level);
            return this.cloudRiderEntity.getTarget() == null && (y > targetLevel + 4 || y < targetLevel - 4);
        }

        @Override
        public void start() {
            double x = this.cloudRiderEntity.getX();
            double z = this.cloudRiderEntity.getZ();
            this.cloudRiderEntity.getMoveControl().setWantedPosition(x, getTargetLevel(this.cloudRiderEntity.level), z, 1);
        }

    }

    static class CloudRiderNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

        public CloudRiderNearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, Predicate<LivingEntity> pTargetPredicate) {
            this(pMob, pTargetType, 0, true, false, pTargetPredicate);
        }

        public CloudRiderNearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
            super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pTargetPredicate);
        }

        @Override
        protected @NotNull AABB getTargetSearchArea(double pTargetDistance) {
            return this.mob.getBoundingBox().inflate(pTargetDistance);
        }

        @Override
        protected void findTarget() {
            // When follow range gets changed, the targeting conditions object needs to be updated
            if (this.mob.tickCount % TICKS_PER_SECOND == 0) {
                this.targetConditions = this.targetConditions.range(this.getFollowDistance());
            }
            super.findTarget();
        }
    }

    // HurtByTargetGoal only applies to PathFinderMobs, so I had to manually make a class for Cloud Riders
    static class CloudRiderEntityHurtByTargetGoal extends TargetGoal {

        private final CloudRiderEntity cloudRiderEntity;
        private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
        private int timestamp;

        public CloudRiderEntityHurtByTargetGoal(CloudRiderEntity cloudRiderEntity, boolean pMustSee) {
            super(cloudRiderEntity, pMustSee);
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            int i = this.mob.getLastHurtByMobTimestamp();
            LivingEntity livingEntity = this.mob.getLastHurtByMob();
            if (i != this.timestamp && livingEntity != null && !(livingEntity instanceof CloudRiderEntity)) {
                if (livingEntity.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                    return false;
                } else {
                    // If it cannot see target, it will still retaliate with a single shot

                    // (Kind of) passive in the end; when attacked by a player, it will only retaliate once.
                    // I made it like this in case the player accidentally hits the cloud rider in the end; the player
                    // would otherwise have little to no means of escape without an elytra.
                    // Also the cloud rider would be too distracted by either the Ender Dragon or the beauty of the End
                    // dimension to care.
                    if ((livingEntity.distanceTo(this.cloudRiderEntity) > this.cloudRiderEntity.getAttributeValue(Attributes.FOLLOW_RANGE) ||
                            (livingEntity instanceof Player player && player.level.dimension() == Level.END)) &&
                            (!(livingEntity instanceof Player player) || !player.isCreative())) {
                        this.cloudRiderEntity.shootTarget(this.cloudRiderEntity, livingEntity,
                                (float) this.cloudRiderEntity.getAttributeValue(Attributes.ATTACK_DAMAGE), false);
                        this.cloudRiderEntity.setLastHurtByMob(null);
                        return false;
                    }
                    return this.canAttack(livingEntity, HURT_BY_TARGETING);
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

    public enum Type implements StringRepresentable {
        WHITE(0, "white"),
        GRAY(1, "gray");

        private final int id;
        private final String name;
        public static final StringRepresentable.EnumCodec<CloudRiderEntity.Type> CODEC = StringRepresentable.fromEnum(CloudRiderEntity.Type::values);
        private static final IntFunction<CloudRiderEntity.Type> BY_ID = ByIdMap.continuous(CloudRiderEntity.Type::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        Type(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static CloudRiderEntity.Type byId(int index) {
            return BY_ID.apply(index);
        }

        public static CloudRiderEntity.Type byName(String name) {
            return CODEC.byName(name, WHITE);
        }

    }

}
