package com.leecrafts.cloudrider.client.cloudrider;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CloudRiderRenderer extends GeoEntityRenderer<CloudRiderEntity> {

    public CloudRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CloudRiderModel(new ResourceLocation(CloudRider.MODID, "geo/cloud_rider.geo.json")));
        this.shadowRadius = 0.75f;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CloudRiderEntity animatable) {
        if (animatable.getType() == ModEntityTypes.WHITE_CLOUD_RIDER.get()) {
            return new ResourceLocation(CloudRider.MODID, "textures/entity/white_cloud_rider_texture.png");
        }
        else {
            return new ResourceLocation(CloudRider.MODID, "textures/entity/gray_cloud_rider_texture.png");
        }
    }

}
