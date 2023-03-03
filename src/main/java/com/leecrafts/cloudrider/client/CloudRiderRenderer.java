package com.leecrafts.cloudrider.client;

import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CloudRiderRenderer extends GeoEntityRenderer<CloudRiderEntity> {

    public CloudRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CloudRiderModel());
        this.shadowRadius = 1.5f;
    }

}
