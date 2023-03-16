package com.leecrafts.cloudrider.client.cloudsteed;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CloudSteedModel extends GeoModel<CloudSteedEntity> {

    private static final ResourceLocation WHITE_CLOUD_STEED_TEXUTRE = new ResourceLocation(CloudRider.MODID, "textures/entity/white_cloud_steed_texture.png");
    private static final ResourceLocation GRAY_CLOUD_STEED_TEXUTRE = new ResourceLocation(CloudRider.MODID, "textures/entity/gray_cloud_steed_texture.png");
    @Override
    public ResourceLocation getModelResource(CloudSteedEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "geo/cloud_steed.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CloudSteedEntity animatable) {
        if (animatable.getVariant() == CloudSteedEntity.Type.WHITE) {
            return WHITE_CLOUD_STEED_TEXUTRE;
        }
        else {
            return GRAY_CLOUD_STEED_TEXUTRE;
        }
    }

    @Override
    public ResourceLocation getAnimationResource(CloudSteedEntity animatable) {
        return null;
    }

}
