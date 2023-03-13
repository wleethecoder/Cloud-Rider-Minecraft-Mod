package com.leecrafts.cloudrider.client.cloudsteed;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CloudSteedModel extends GeoModel<CloudSteedEntity> {

    @Override
    public ResourceLocation getModelResource(CloudSteedEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "geo/cloud_steed.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CloudSteedEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "textures/entity/cloud_steed_texture.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CloudSteedEntity animatable) {
        return null;
    }

}
