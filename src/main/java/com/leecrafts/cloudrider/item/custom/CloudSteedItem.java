package com.leecrafts.cloudrider.item.custom;

import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class CloudSteedItem extends Item {

    private final CloudSteedEntity.Type type;

    public CloudSteedItem(CloudSteedEntity.Type type, Properties pProperties) {
        super(pProperties);
        this.type = type;
    }

    // Regardless of where the player is looking, using this item places a cloud steed at (not below) the player's feet
    // Therefore, a cloud steed can be placed midair
    // If the player is flying with an elytra and places down a cloud steed, it automatically rides the vehicle
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        HitResult hitresult = Item.getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.ANY);

        CloudSteedEntity cloudSteedEntity = new CloudSteedEntity(pPlayer.getX(), pPlayer.getY() + 1e-5, pPlayer.getZ(), pLevel);
        cloudSteedEntity.setVariant(this.type);
        cloudSteedEntity.setYRot(pPlayer.getYRot());

        if (!pLevel.isClientSide) {
            pLevel.addFreshEntity(cloudSteedEntity);
            pLevel.gameEvent(pPlayer, GameEvent.ENTITY_PLACE, hitresult.getLocation());
            if (!pPlayer.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            if (pPlayer.isFallFlying()) {
                pPlayer.startRiding(cloudSteedEntity, true);
            }
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide);
    }

    // Cloud steed entities can be shot out from dispensers.
    public static class CloudSteedDispenseItemBehavior extends DefaultDispenseItemBehavior {

        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
        private final CloudSteedEntity.Type type;

        public CloudSteedDispenseItemBehavior(CloudSteedEntity.Type type) {
            this.type = type;
        }

        @Override
        protected @NotNull ItemStack execute(@NotNull BlockSource pSource, @NotNull ItemStack pStack) {
            Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
            Level level = pSource.getLevel();
            double x = pSource.x() + (double)((float)direction.getStepX() * 1.25f);
            double y = pSource.y() + (double)((float)direction.getStepY() * 1.25f);
            double z = pSource.z() + (double)((float)direction.getStepZ() * 1.25f);
            BlockPos blockPos = pSource.getPos().relative(direction);
            CloudSteedEntity cloudSteedEntity = new CloudSteedEntity(x, y, z, level);
            cloudSteedEntity.setVariant(this.type);
            cloudSteedEntity.setYRot(direction.toYRot());
            if (!level.getBlockState(blockPos).isAir()) {
                return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
            }
            cloudSteedEntity.setPos(x, y, z);
            level.addFreshEntity(cloudSteedEntity);
            pStack.shrink(1);
            return pStack;
        }

    }

}
