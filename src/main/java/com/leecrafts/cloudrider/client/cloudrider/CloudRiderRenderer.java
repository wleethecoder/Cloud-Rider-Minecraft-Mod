package com.leecrafts.cloudrider.client.cloudrider;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CloudRiderRenderer extends GeoEntityRenderer<CloudRiderEntity> {

    public CloudRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CloudRiderModel(new ResourceLocation(CloudRider.MODID, "geo/cloud_rider.geo.json")));
        this.shadowRadius = 0.75f;
    }

}
