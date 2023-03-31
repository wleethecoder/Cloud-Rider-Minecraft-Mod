package com.leecrafts.cloudrider.item.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class AbstractMistySpongeItem extends BlockItem {

    private final boolean isGray;

    public AbstractMistySpongeItem(Block pBlock, Properties pProperties, boolean isGray) {
        super(pBlock, pProperties);
        this.isGray = isGray;
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext pContext) {
        InteractionResult interactionResult = super.place(pContext);
        if (interactionResult != InteractionResult.FAIL) {
            if (pContext.getLevel() instanceof ServerLevel serverLevel) {
                EntityType<CloudRiderEntity> cloudRiderVariant = !this.isGray ? ModEntityTypes.WHITE_CLOUD_RIDER.get() :
                        ModEntityTypes.GRAY_CLOUD_RIDER.get();
                CloudRiderEntity cloudRiderEntity = cloudRiderVariant.spawn(serverLevel, pContext.getClickedPos().above(), MobSpawnType.MOB_SUMMONED);
                ItemStack itemStack = pContext.getItemInHand();
                if (cloudRiderEntity != null) {
                    if (itemStack.hasCustomHoverName()) {
                        cloudRiderEntity.setCustomName(itemStack.getHoverName());
                    }
                    cloudRiderEntity.setPersistenceRequired();
                }
            }
        }
        return interactionResult;
    }

    @Override
    public @NotNull String getDescriptionId() {
        return !this.isGray ? "Misty Sponge" : "Foggy Sponge";
    }

}
