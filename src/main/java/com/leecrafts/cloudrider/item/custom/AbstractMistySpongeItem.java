package com.leecrafts.cloudrider.item.custom;

import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
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

    // Placing down a misty/foggy sponge summons a white/gray cloud rider above. The sponge turns back to normal.
    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext pContext) {
        InteractionResult interactionResult = super.place(pContext);
        if (interactionResult != InteractionResult.FAIL) {
            if (pContext.getLevel() instanceof ServerLevel serverLevel) {
                CloudRiderEntity cloudRiderEntity = ModEntityTypes.CLOUD_RIDER.get().spawn(serverLevel, pContext.getClickedPos().above(), MobSpawnType.MOB_SUMMONED);
                ItemStack itemStack = pContext.getItemInHand();
                if (cloudRiderEntity != null) {
                    if (this.isGray) {
                        cloudRiderEntity.setVariant(CloudRiderEntity.Type.GRAY);
                    }
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
