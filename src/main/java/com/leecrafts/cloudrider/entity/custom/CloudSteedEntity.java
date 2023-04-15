package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.capability.ModCapabilities;
import com.leecrafts.cloudrider.capability.cloudsteedentity.CloudSteedEntityCap;
import com.leecrafts.cloudrider.capability.lightning.LightningCap;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.item.ModItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.IntFunction;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class CloudSteedEntity extends Entity implements GeoAnimatable, VariantHolder<CloudSteedEntity.Type> {

    // A fun vehicle that floats in the direction the passenger is looking
    // Cloud riders become neutral towards the passenger
    // Gray cloud steeds strike (grounded) enemy mobs below it with lightning

    private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(CloudSteedEntity.class, EntityDataSerializers.INT);
    private final double MAX_SPEED_PER_SECOND = 20;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CloudSteedEntity(EntityType<? extends CloudSteedEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CloudSteedEntity(double xPos, double yPos, double zPos, Level level) {
        this(ModEntityTypes.CLOUD_STEED.get(), level);
        this.setPos(xPos, yPos, zPos);
    }

    @Override
    public void setNoGravity(boolean pNoGravity) {
        super.setNoGravity(true);
    }

    @Override
    public boolean canCollideWith(@NotNull Entity pEntity) {
        return (pEntity.canBeCollidedWith() || pEntity.isPushable()) && !this.isPassengerOfSameVehicle(pEntity);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    // Exposure to (vanilla) lightning bolts that are not its own, lightning bolt projectiles, lava, and the Nether
    // destroys the cloud steed
    // When hit with anything else, it transfers the damage to its passenger
    // Otherwise, if it has no passenger, than anything can destroy it
    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        }
        if (!this.level.isClientSide && !this.isRemoved()) {
            if (pSource == DamageSource.LAVA ||
                    pSource == DamageSource.LIGHTNING_BOLT ||
                    (pSource.getDirectEntity() != null && pSource.getDirectEntity().getType() == ModEntityTypes.LIGHTNING_BOLT_PROJECTILE.get())) {
                CloudRiderEntity.vaporizeParticles(this);
                this.playSound(SoundEvents.FIRE_EXTINGUISH);
                this.destroy();
                return true;
            }
            Entity passenger = this.getControllingPassenger();
            if (passenger != null) {
                return passenger.hurt(pSource, pAmount);
            }
            this.destroy();
        }
        return true;
    }

    private void destroy() {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(this.getDropItem());
        }
        this.discard();
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        super.tick();

        // can't ride it in the Nether
        if (this.level.dimensionType().ultraWarm()) {
            this.hurt(DamageSource.LAVA, 0);
        }

        if (this.level.isClientSide && this.getVariant() == Type.GRAY) {
            CloudRiderEntity.grayCloudParticles(this);
        }
        if (this.isControlledByLocalInstance()) {

            // Prevents an occasional clipping bug
            if (this.isOnGround()) {
//                System.out.println("preventing clipping ☝️🤓");
                this.setPos(this.getX(), this.getY() + 1e-5, this.getZ());
            }

            // This fixes the bug that occasionally makes the steed "disappear" when the passenger dismounts
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());

            if (this.level.isClientSide) {
                this.controlSteed();
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        // Gray cloud steeds summons thunder on enemy mobs below it
        // The mob has to be on the ground or in the water, so this will probably not work on cloud riders!
        else if (this.getControllingPassenger() instanceof LivingEntity passenger && this.getVariant() == Type.GRAY) {
            if (this.tickCount % 10 == 0) {
                for (int i = this.getBlockY(); i >= -64; i--) {
                    BlockPos blockPos = this.blockPosition().atY(i);
                    if (!this.level.getBlockState(blockPos).isAir()) {
                        AABB aabb = (new AABB(blockPos.above())).inflate(2.5, 1, 2.5);
                        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, aabb);
                        boolean shouldStrike = false;
                        for (LivingEntity livingEntity : list) {
                            if (livingEntity instanceof Enemy ||
                                    (passenger.getLastHurtByMob() != null && passenger.getLastHurtByMob().is(livingEntity))) {
                                shouldStrike = true;
                            }
                        }
                        if (shouldStrike) {
                            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this.level);
                            if (lightningBolt != null) {
                                lightningBolt.getCapability(ModCapabilities.LIGHTNING_CAPABILITY).ifPresent(iLightningCap -> {
                                    LightningCap lightningCap = (LightningCap) iLightningCap;
                                    lightningCap.fromGrayCloudSteed = true;
                                });
                                lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos.above()));
                                lightningBolt.setCause(this.getControllingPassenger() instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                                this.level.addFreshEntity(lightningBolt);
                            }
                        }
                        if (this.level.getBlockState(blockPos).getMaterial().isSolid()) {
                            break;
                        }
                    }
                }
            }
        }
        else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.checkInsideBlocks();
    }

    // Controls:
    // W moves the vehicle in the direction its passenger is looking
    // A/D moves the vehicle left/right
    // S moves the vehicle in the opposite direction its passenger is looking
    // Space cancels the vehicle's vertical movement, easing control of horizontal movement
    private void controlSteed() {
        if (this.isVehicle() && this.getControllingPassenger() instanceof LocalPlayer localPlayer) {
            // Update steed's rotation so that the steed faces where the passenger faces
            this.setYRot(localPlayer.getYHeadRot());

            double slowdownFactor = 0.93;
            Vec3 deltaMovement = this.getDeltaMovement();
            if (noMoveKeysPressed(localPlayer)) {
                this.setDeltaMovement(deltaMovement.scale(slowdownFactor));
            }
            else if (noWASD(localPlayer)) {
                this.setDeltaMovement(deltaMovement.multiply(slowdownFactor, 0, slowdownFactor));
            }
            else {
                Vec3 vec3 = localPlayer.getViewVector(1);
                if (localPlayer.input.down) {
                    vec3 = vec3.reverse();
                }
                else if (localPlayer.input.left) {
                    vec3 = strictLeft(vec3, localPlayer);
                }
                else if (localPlayer.input.right) {
                    vec3 = strictRight(vec3, localPlayer);
                }

                if (localPlayer.input.jumping) {
                    vec3 = toXZ(vec3);
                    if (Math.abs(vec3.x) < 1e-15 && Math.abs(vec3.z) < 1e-15) {
                        if (localPlayer.input.up) {
                            vec3 = calculateHorizontalViewVector(localPlayer.getViewYRot(1));
                        }
                        else if (localPlayer.input.down) {
                            vec3 = calculateHorizontalViewVector(localPlayer.getViewYRot(1)).reverse();
                        }
                    }
                }
                vec3 = vec3.normalize();

                Vec3 resultantVector = deltaMovement.add(vec3.scale(0.04));
                double maxSpeed = MAX_SPEED_PER_SECOND / TICKS_PER_SECOND;
                this.setDeltaMovement(resultantVector.length() < maxSpeed ?
                        resultantVector : resultantVector.normalize().scale(maxSpeed));

                if (localPlayer.input.jumping) {
                    this.setDeltaMovement(toXZ(this.getDeltaMovement()));
                }
            }

        }
    }

    private boolean noMoveKeysPressed(LocalPlayer localPlayer) {
        return noWASD(localPlayer) && !localPlayer.input.jumping;
    }
    private boolean noWASD(LocalPlayer localPlayer) {
        return !localPlayer.input.up && !localPlayer.input.down && !localPlayer.input.left && !localPlayer.input.right;
    }

    private Vec3 strictLeft(Vec3 vec3, LocalPlayer passenger) {
        return left(Math.abs(vec3.normalize().y) != 1.0 ?
                vec3 : calculateHorizontalViewVector(passenger.getViewYRot(1)));
    }

    private Vec3 strictRight(Vec3 vec3, LocalPlayer passenger) {
        return strictLeft(vec3, passenger).multiply(-1, 1, -1);
    }

    private Vec3 left(Vec3 vec3) {
        return new Vec3(vec3.z, 0, -vec3.x);
    }

    private Vec3 toXZ(Vec3 vec3) {
        return vec3.multiply(1, 0, 1);
    }

    private Vec3 calculateHorizontalViewVector(float yRot) {
        float yRotRadians = -yRot * ((float)Math.PI / 180F);
        float x = Mth.sin(yRotRadians);
        float z = Mth.cos(yRotRadians);
        return new Vec3(x, 0, z);
    }

    @Override
    public void positionRider(@NotNull Entity pPassenger) {
        if (this.hasPassenger(pPassenger)) {
            pPassenger.setPos(this.getX(), this.getY(), this.getZ());
        }
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (pPlayer.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!pPlayer.level.isClientSide) {
            return pPlayer.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
        if (!this.isPassenger() && pOnGround) {
            this.resetFallDistance();
        }
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity pPassenger) {
        return this.getControllingPassenger() == null;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return super.getFirstPassenger();
    }

    // There was a bug that occurred whenever the player dismounted a moving cloud steed.
    // The vehicle's client and server position would become out of sync, causing it to teleport to another location instead of under the player's feet.
    // Additionally, the player would get stuck and experience a camera "jitter" effect.
    // Discarding and replacing the vehicle was the only solution I found.
    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity pPassenger) {
        Vec3 vec3 = super.getDismountLocationForPassenger(pPassenger);
        this.getCapability(ModCapabilities.CLOUD_STEED_ENTITY_CAPABILITY).ifPresent(iCloudSteedEntityCap -> {
            CloudSteedEntityCap cloudSteedEntityCap = (CloudSteedEntityCap) iCloudSteedEntityCap;
            // For some reason, when the player "dismounts" the vehicle by logging off, calling discard() does not actually discard the vehicle.
            // Therefore, in this situation, another steed shouldn't replace it.
            if (!cloudSteedEntityCap.playerPassengerHadLoggedOut) {
                CloudSteedEntity cloudSteedEntity = new CloudSteedEntity(this.getX(), this.getY(), this.getZ(), this.level);
                cloudSteedEntity.setVariant(this.getVariant());
                cloudSteedEntity.setYRot(this.getYRot());
                this.level.addFreshEntity(cloudSteedEntity);
                this.discard();
            }
            else {
                cloudSteedEntityCap.playerPassengerHadLoggedOut = false;
            }
            // this.discard();
        });
        return vec3;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }

    public Item getDropItem() {
        return this.getVariant() == Type.WHITE ? ModItems.WHITE_CLOUD_STEED_ITEM.get() : ModItems.GRAY_CLOUD_STEED_ITEM.get();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_TYPE, 0);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        this.setVariant(CloudSteedEntity.Type.byName(pCompound.getString("Type")));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        pCompound.putString("Type", this.getVariant().getSerializedName());
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
        return ((Entity) o).tickCount;
    }

    @Override
    public void setVariant(@NotNull Type pVariant) {
        this.entityData.set(DATA_ID_TYPE, pVariant.getId());
    }

    @Override
    public @NotNull Type getVariant() {
        return CloudSteedEntity.Type.byId(this.entityData.get(DATA_ID_TYPE));
    }

    public enum Type implements StringRepresentable {
        WHITE(0, "white"),
        GRAY(1, "gray");

        private final int id;
        private final String name;
        public static final StringRepresentable.EnumCodec<CloudSteedEntity.Type> CODEC = StringRepresentable.fromEnum(CloudSteedEntity.Type::values);
        private static final IntFunction<CloudSteedEntity.Type> BY_ID = ByIdMap.continuous(CloudSteedEntity.Type::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

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

        public static CloudSteedEntity.Type byId(int index) {
            return BY_ID.apply(index);
        }

        public static CloudSteedEntity.Type byName(String name) {
            return CODEC.byName(name, WHITE);
        }

    }

}
