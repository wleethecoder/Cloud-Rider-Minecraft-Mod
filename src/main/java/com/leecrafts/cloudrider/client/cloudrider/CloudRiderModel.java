package com.leecrafts.cloudrider.client.cloudrider;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class CloudRiderModel extends DefaultedEntityGeoModel<CloudRiderEntity> {

    private static final ResourceLocation WHITE_CLOUD_RIDER_TEXUTRE = new ResourceLocation(CloudRider.MODID, "textures/entity/white_cloud_rider_texture.png");
    private static final ResourceLocation GRAY_CLOUD_RIDER_TEXUTRE = new ResourceLocation(CloudRider.MODID, "textures/entity/gray_cloud_rider_texture.png");

    public CloudRiderModel(ResourceLocation assetSubpath) {
        super(assetSubpath, true);
    }

    @Override
    public ResourceLocation getModelResource(CloudRiderEntity object) {
        return new ResourceLocation(CloudRider.MODID, "geo/cloud_rider.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CloudRiderEntity animatable) {
        return animatable.getVariant() == CloudRiderEntity.Type.WHITE ? WHITE_CLOUD_RIDER_TEXUTRE : GRAY_CLOUD_RIDER_TEXUTRE;
    }

    @Override
    public ResourceLocation getAnimationResource(CloudRiderEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "animations/cloud_rider.animation.json");
    }

}
