package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
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
import static net.minecraft.world.level.block.Blocks.*;

public class LightningBoltProjectileEntity extends Projectile implements GeoAnimatable {

    private float damage;
    private int life;
    private boolean isCharged;
    private final double LIFE_SPAN = 4;
    private final double MAX_CURVE_ANGLE = 45;
    public int ambientSoundTime;
    private LivingEntity target;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LightningBoltProjectileEntity(EntityType<? extends LightningBoltProjectileEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public LightningBoltProjectileEntity(Level level, LivingEntity shooter, float damage, boolean isCharged) {
        this(ModEntityTypes.LIGHTNING_BOLT_PROJECTILE.get(), level);
        this.setOwner(shooter);
        Vec3 vec3 = shooter.getViewVector(1);
        this.setPos( shooter.getX() + vec3.x * 0.6, shooter.getY(0.5), shooter.getZ() + vec3.z * 0.6);
        this.damage = damage;
        this.isCharged = isCharged;
        this.resetAmbientSoundTime();
    }

    public void shoot(LivingEntity target, float velocity) {
        this.target = target;
        double xDir = this.target.getX() - this.getX();
        double yDir = this.target.getY(0.25) - this.getY();
        double zDir = this.target.getZ() - this.getZ();
        this.shoot(xDir, yDir, zDir, velocity, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.life >= LIFE_SPAN * TICKS_PER_SECOND) {
            this.discard();
        }
        this.life++;
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitResult)) {
            this.onHit(hitResult);
        }

        Vec3 vec3 = this.getDeltaMovement();
        // For each tick, uncharged shots have a 16.7% chance of homing in on the target
        if (this.target != null && (this.isCharged || this.random.nextInt(6) == 0)) {
            double xDir = this.target.getX() - this.getX();
            double yDir = this.target.getY(0.25) - this.getY();
            double zDir = this.target.getZ() - this.getZ();
            Vec3 newVec3 = new Vec3(xDir, yDir, zDir);
            newVec3 = newVec3.normalize().scale(vec3.length());
            double angle = Math.toDegrees(Math.acos(vec3.dot(newVec3) / (vec3.length() * newVec3.length())));
            if (!Double.isNaN(angle) && angle < MAX_CURVE_ANGLE) {
                vec3 = newVec3;
            }
        }

        double xNew = this.getX() + vec3.x;
        double yNew = this.getY() + vec3.y;
        double zNew = this.getZ() + vec3.z;
        this.updateRotation();
        this.setDeltaMovement(vec3);
        this.setPos(xNew, yNew, zNew);

        if (!this.level.isClientSide) {
            SimpleParticleType simpleParticleType = ParticleTypes.ELECTRIC_SPARK;
            double particleSpeed = 0.7;
            if (this.isCharged) {
                simpleParticleType = ParticleTypes.END_ROD;
                particleSpeed = 0.05;
            }
            ((ServerLevel) this.level).sendParticles(
                    simpleParticleType, this.getX(), this.getY(), this.getZ(),
                    3, 0, 0, 0, particleSpeed
            );
        }

        if (this.random.nextInt(3) < this.ambientSoundTime++) {
            this.playAmbientSound();
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        Entity shooter = this.getOwner();
        Entity target = pResult.getEntity();
        float damage;
        if (!this.level.isClientSide) {
            DamageSource damageSource = shooter instanceof LivingEntity ? DamageSource.indirectMobAttack(this, (LivingEntity) shooter) :
                    new IndirectEntityDamageSource("lightningBoltProjectile", this, this);
            damageSource = damageSource.setProjectile();

            // DamageSource::setScalesWithDifficulty() will not work because it will always return false (LightningBoltProjectileEntity is not a LivingEntity)
            damage = this.scaleWithDifficulty(this.damage);

            // Charged projectiles always create an area of electricity
            boolean mustElectrify = this.isCharged;
            if (target instanceof Player || target instanceof Mob || target instanceof ArmorStand) {
                // If the lightning bolt projectile hits a wet (or conductible) entity, then it creates an area of electricity
                if (target.isInWaterRainOrBubble() || target instanceof IronGolem) {
                    mustElectrify = true;
                }
                else {
                    Iterable<ItemStack> armorSlots = target.getArmorSlots();
                    for (ItemStack itemStack : armorSlots) {
                        if (this.isConductibleArmor(itemStack.getItem())) {
                            mustElectrify = true;
                            break;
                        }
                    }
                }

                // Lightning bolt projectiles do twice the damage towards cloud riders and water creatures
                if (target instanceof CloudRiderEntity ||
                        target instanceof WaterAnimal ||
                        target instanceof Guardian) {
                    damage *= 2;
                }
            }

            // The Ender Dragon can be damaged by a lightning bolt projectile because it becomes registered as an "explosion"
            else if (target instanceof EnderDragonPart) {
                damageSource = damageSource.setExplosion();
                damage = (damage - 1) * 4;
            }

            if (mustElectrify) {
                this.electrify(target.getX(), target.getY(0.5), target.getZ());
            }
            target.hurt(damageSource, damage);
            if (shooter instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity) shooter, target);
            }
        }
    }

    private float scaleWithDifficulty(float damage) {
        return switch (this.level.getDifficulty()) {
            case PEACEFUL -> 0.0f;
            case EASY -> Math.min(damage / 2.0f + 1.0f, damage);
            case NORMAL -> damage;
            default -> damage * 1.5f;
        };
    }

    // If the lightning bolt projectile hits a conductible or wet block, then it creates an area of electricity
    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if (!this.level.isClientSide &&
                (this.isCharged || this.isInWaterRainOrBubble() || isConductibleBlock(this.level.getBlockState(pResult.getBlockPos())))) {
            this.electrify();
        }
    }

    private void electrify(double x, double y, double z) {
        ElectricAreaEffectCloud electricAreaEffectCloud =
                new ElectricAreaEffectCloud(this.level, x, y, z);
        if (this.getOwner() instanceof LivingEntity livingEntity) {
            electricAreaEffectCloud.setOwner(livingEntity);
        }
        this.level.addFreshEntity(electricAreaEffectCloud);
    }

    private void electrify() {
        this.electrify(this.getX(), this.getY(), this.getZ());
    }

    // Since it's based on gold, netherite may be a good conductor, but I think it would be unfair if netherite armor
    // causes an area of electricity upon being hit
    private boolean isConductibleArmor(Item item) {
        return item instanceof ArmorItem armorItem &&
                (armorItem.getMaterial() == ArmorMaterials.IRON ||
                        armorItem.getMaterial() == ArmorMaterials.CHAIN ||
                        armorItem.getMaterial() == ArmorMaterials.GOLD ||
                        armorItem.getMaterial() == ArmorMaterials.IRON);
    }

    private boolean isConductibleBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        return (material == Material.METAL || material == Material.HEAVY_METAL) &&
                block != LAPIS_BLOCK &&
                block != DIAMOND_BLOCK &&
                block != BREWING_STAND &&
                block != EMERALD_BLOCK &&
                block != REDSTONE_BLOCK &&
                block != NETHERITE_BLOCK &&
                block != ANCIENT_DEBRIS &&
                block != GRINDSTONE &&
                block != LODESTONE;
    }

    @Override
    protected void onHit(@NotNull HitResult pResult) {
        super.onHit(pResult);
        if (!this.level.isClientSide) {
            float volume = 1.0f;
            if (this.isCharged) {
                ((ServerLevel) this.level).sendParticles(
                        ParticleTypes.END_ROD, pResult.getLocation().x, pResult.getLocation().y, pResult.getLocation().z,
                        150, 0, 0, 0, 0.7
                );
                volume = 10.0f;
            }
            this.playSound(SoundEvents.GENERIC_EXPLODE, volume, 1.0f);
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        Entity shooter = this.getOwner();
        return super.canHitEntity(pTarget) && (shooter == null || !pTarget.is(shooter));
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
        return ((Entity) o).tickCount;
    }

    private void playAmbientSound() {
        this.playSound(
                ModSounds.LIGHTNING_BOLT_PROJECTILE_AMBIENT.get(), 1.0f,
                (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        this.resetAmbientSoundTime();
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -3;
    }

}
