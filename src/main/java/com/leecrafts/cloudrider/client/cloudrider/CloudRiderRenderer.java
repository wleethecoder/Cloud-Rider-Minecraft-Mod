package com.leecrafts.cloudrider.client.cloudrider;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CloudRiderRenderer extends GeoEntityRenderer<CloudRiderEntity> {

    public CloudRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CloudRiderModel());
        this.shadowRadius = 1.5f;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CloudRiderEntity instance) {
        return new ResourceLocation(CloudRider.MODID, "textures/entity/cloud_rider_texture.png");
    }

}
