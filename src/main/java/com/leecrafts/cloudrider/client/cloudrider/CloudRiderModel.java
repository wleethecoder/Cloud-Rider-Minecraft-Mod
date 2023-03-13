package com.leecrafts.cloudrider.client.cloudrider;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class CloudRiderModel extends DefaultedEntityGeoModel<CloudRiderEntity> {

    public CloudRiderModel(ResourceLocation assetSubpath) {
        super(assetSubpath, true);
    }

    @Override
    public ResourceLocation getModelResource(CloudRiderEntity object) {
        return new ResourceLocation(CloudRider.MODID, "geo/cloud_rider.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CloudRiderEntity object) {
        return new ResourceLocation(CloudRider.MODID, "textures/entity/cloud_rider_texture.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CloudRiderEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "animations/cloud_rider.animation.json");
    }

}
