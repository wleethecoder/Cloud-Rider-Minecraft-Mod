package com.leecrafts.cloudrider.entity.custom;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class CloudRiderEntity extends FlyingMob implements GeoAnimatable {

    public static final int TICKS_PER_SECOND = 20;
    public static final int CLOUD_LEVEL = 192;
    public static final double MOVEMENT_SPEED = 45;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("animation.cloud_rider.idle_draft");

    public CloudRiderEntity(EntityType<? extends CloudRiderEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 10;
        this.moveControl = new CloudRiderEntity.CloudRiderMoveControl(this);
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30)
//                .add(Attributes.MOVEMENT_SPEED, 1.0f)
//                .add(Attributes.ATTACK_SPEED, 2.0f)
//                .add(Attributes.ATTACK_DAMAGE, 6.0f)
                .add(Attributes.FOLLOW_RANGE, 32).build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new CloudRiderEntity.ChaseTargetGoal(this));
        this.goalSelector.addGoal(5, new CloudRiderEntity.MoveToCloudLevelGoal(this));
//        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

//    private <E extends GeoAnimatable> PlayState predicate(AnimationState event) {
//        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.cloud_rider.idle_draft",
//                ILoopType.EDefaultLoopTypes.LOOP));
//        return PlayState.CONTINUE;
//    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "idle", 5, state -> state.setAndContinue(IDLE))
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }

    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
        return SoundEvents.PLAYER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    static class CloudRiderMoveControl extends MoveControl {

        private final CloudRiderEntity cloudRiderEntity;

        public CloudRiderMoveControl(CloudRiderEntity cloudRiderEntity) {
            super(cloudRiderEntity);
            this.cloudRiderEntity = cloudRiderEntity;
        }

        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.getWantedX() - this.cloudRiderEntity.getX(),
                        this.getWantedY() - this.cloudRiderEntity.getY(),
                        this.getWantedZ() - this.cloudRiderEntity.getZ());
                double travelDistance = vec3.length();
                if (travelDistance > 3) {
                    vec3 = vec3.normalize();
                    this.cloudRiderEntity.setDeltaMovement(vec3.scale(MOVEMENT_SPEED / TICKS_PER_SECOND));
                }
                else {
//                    System.out.println("Movement goal already reached");
                    this.operation = Operation.WAIT;
                }
            }
            else if (this.operation == Operation.WAIT) {
                this.cloudRiderEntity.setDeltaMovement(this.cloudRiderEntity.getDeltaMovement().scale(0.1));
            }
        }

    }

    static class ChaseTargetGoal extends Goal {

        private final CloudRiderEntity cloudRiderEntity;

        public ChaseTargetGoal(CloudRiderEntity cloudRiderEntity) {
            this.cloudRiderEntity = cloudRiderEntity;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return this.cloudRiderEntity.getTarget() != null;
        }

        public void tick() {
            LivingEntity target = this.cloudRiderEntity.getTarget();
            if (target != null) {
                this.cloudRiderEntity.getMoveControl().setWantedPosition(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        1
                );
            }
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

        public boolean canUse() {
            boolean isAttacking = this.cloudRiderEntity.getTarget() != null;
            double y = this.cloudRiderEntity.getY();
            return !isAttacking && (y > CLOUD_LEVEL + 4 || y < CLOUD_LEVEL - 4) ||
                    isAttacking && y < CLOUD_LEVEL - 48;
        }

        public void start() {
            double x = this.cloudRiderEntity.getX();
            double z = this.cloudRiderEntity.getZ();
            this.cloudRiderEntity.getMoveControl().setWantedPosition(x, CLOUD_LEVEL, z, 1);
//            System.out.println("[" + this.cloudRiderEntity.getMoveControl().getWantedX() + ", " +
//                    this.cloudRiderEntity.getMoveControl().getWantedY() + ", " +
//                    this.cloudRiderEntity.getMoveControl().getWantedZ() + "]");
        }

    }

}
