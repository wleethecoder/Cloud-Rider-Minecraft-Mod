package com.leecrafts.cloudrider.client.cloudrider;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;

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
        if (animatable.getType() == ModEntityTypes.WHITE_CLOUD_RIDER.get()) {
            return WHITE_CLOUD_RIDER_TEXUTRE;
        }
        return GRAY_CLOUD_RIDER_TEXUTRE;
    }

    @Override
    public ResourceLocation getAnimationResource(CloudRiderEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "animations/cloud_rider.animation.json");
    }

}
