package com.leecrafts.cloudrider.entity.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
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
    private double shooterX;
    private double shooterY;
    private double shooterZ;
    private int life;
    private final double LIFE_SPAN = 4;
    private final double MAX_CURVE_ANGLE = 45;
    private LivingEntity target;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LightningBoltProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public LightningBoltProjectileEntity(Level level, LivingEntity shooter, float damage) {
        this(ModEntityTypes.LIGHTNING_BOLT_PROJECTILE.get(), level);
        this.setOwner(shooter);
        Vec3 vec3 = shooter.getViewVector(1);
        this.shooterX = shooter.getX() + vec3.x;
        this.shooterY = shooter.getY(0.5);
        this.shooterZ = shooter.getZ() + vec3.z;
        this.setPos(this.shooterX, this.shooterY, this.shooterZ);
        this.damage = damage;
    }

    public void shoot(LivingEntity target, float velocity) {
        this.target = target;
        double xDir = this.target.getX() - this.shooterX;
        double yDir = this.target.getY() - this.shooterY;
        double zDir = this.target.getZ() - this.shooterZ;
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0f, 1.0f);
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
        if (this.target != null && this.random.nextInt(6) == 0) {
            double xDir = this.target.getX() - this.getX();
            double yDir = this.target.getY() - this.getY();
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
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity shooter = this.getOwner();
        Entity target = pResult.getEntity();
        float damage = this.damage;
        if (!this.level.isClientSide && (shooter == null || !target.is(shooter))) {
            DamageSource damageSource = shooter instanceof LivingEntity ? DamageSource.indirectMobAttack(this, (LivingEntity) shooter) :
                    new IndirectEntityDamageSource("lightningBoltProjectile", this, this);
            damageSource = damageSource.setProjectile();
            if (target instanceof Player || target instanceof Mob || target instanceof ArmorStand) {
                if (target.isInWaterRainOrBubble() || target instanceof IronGolem) {
                    this.electrify(this.level, target.getX(), target.getY(), target.getZ());
                }
                else {
                    Iterable<ItemStack> armorSlots = target.getArmorSlots();
                    for (ItemStack itemStack : armorSlots) {
                        if (this.isConductibleArmor(itemStack.getItem())) {
                            this.electrify(this.level, target.getX(), target.getY(), target.getZ());
                            break;
                        }
                    }
                }
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

            // TODO test enchant effects
            target.hurt(damageSource, damage);
            if (shooter instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity) shooter, target);
            }
        }
    }

    // TODO water behavior?
    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        Level level = this.level;
        if (!level.isClientSide && isConductibleBlock(level.getBlockState(pResult.getBlockPos()))) {
            this.electrify(level);
        }
    }

    private void electrify(Level level, double x, double y, double z) {
        ElectricAreaEffectCloud electricAreaEffectCloud =
                new ElectricAreaEffectCloud(level, x, y, z);
        if (this.getOwner() instanceof LivingEntity livingEntity) {
            electricAreaEffectCloud.setOwner(livingEntity);
        }
        level.addFreshEntity(electricAreaEffectCloud);
    }

    private void electrify(Level level) {
        this.electrify(level, this.getX(), this.getY(), this.getZ());
    }

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
        return (material == Material.METAL || material == Material.HEAVY_METAL || material == Material.WATER) &&
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
        return ((Entity) o).tickCount;
    }

}
