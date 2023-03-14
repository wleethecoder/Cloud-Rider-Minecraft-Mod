package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class CloudSteedEntity extends Entity implements GeoAnimatable {

    private final double MAX_SPEED_PER_SECOND = 20;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CloudSteedEntity(EntityType<CloudSteedEntity> pEntityType, Level pLevel) {
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
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        }
        if (!this.level.isClientSide && !this.isRemoved()) {
            if (this.hasControllingPassenger()) {
                return Objects.requireNonNull(this.getControllingPassenger()).hurt(pSource, pAmount);
            }
            this.discard();
        }
        return true;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isControlledByLocalInstance()) {

            // This fixes the bug that makes the steed "disappear" when the passenger dismounts
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());

            if (this.level.isClientSide) {
                this.controlSteed();
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.checkInsideBlocks();
    }

    private void controlSteed() {
        if (this.isVehicle()) {
            LocalPlayer localPlayer = (LocalPlayer) getControllingPassenger();
            if (localPlayer != null) {

                // Update steed's rotation so that the steed faces where the passenger faces
                this.setYRot(localPlayer.getYHeadRot());

                if (noWASD(localPlayer)) {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
                }
                else {
                    Vec3 vec3 = localPlayer.getViewVector(1).normalize();
                    if (localPlayer.input.down) {
                        vec3 = vec3.reverse();
                    }
                    else if (localPlayer.input.left) {
                        vec3 = left(vec3, localPlayer);
                    }
                    else if (localPlayer.input.right) {
                        vec3 = right(vec3, localPlayer);
                    }

                    Vec3 resultantVector = this.getDeltaMovement().add(vec3.scale(0.04));
                    double maxSpeed = MAX_SPEED_PER_SECOND / TICKS_PER_SECOND;
                    this.setDeltaMovement(resultantVector.length() < maxSpeed ?
                            resultantVector : resultantVector.normalize().scale(maxSpeed));
                }
            }
        }
    }

    private boolean noWASD(LocalPlayer localPlayer) {
        return !localPlayer.input.up && !localPlayer.input.down && !localPlayer.input.left && !localPlayer.input.right;
    }

    private Vec3 left(Vec3 vec3, LocalPlayer passenger) {
        return rotateCounterClockwise(Math.abs(vec3.normalize().y) != 1.0 ?
                vec3 : calculateHorizontalViewVector(passenger.getViewYRot(1)));
    }

    private Vec3 right(Vec3 vec3, LocalPlayer passenger) {
        return left(vec3, passenger).multiply(-1, 1, -1);
    }

    private Vec3 rotateCounterClockwise(Vec3 vec3) {
        return new Vec3(vec3.z, 0, -vec3.x);
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

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
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
}
