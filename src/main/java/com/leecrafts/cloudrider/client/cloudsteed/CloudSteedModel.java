package com.leecrafts.cloudrider.client.cloudsteed;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CloudSteedModel extends GeoModel<CloudSteedEntity> {

    @Override
    public ResourceLocation getModelResource(CloudSteedEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "geo/cloud_steed.geo.json");
    }

    // Cloud steed becomes translucent when the player rides it (and is in first person view) so that the player can see
    // what's below it
    @Override
    public ResourceLocation getTextureResource(CloudSteedEntity animatable) {
        String path = "textures/entity/";
        if (animatable.getVariant() == CloudSteedEntity.Type.WHITE) {
            path += "white_cloud_steed";
        }
        else {
            path += "gray_cloud_steed";
        }
        if (this.controlledAndFirstPersonView(animatable)) {
            path += "_transparent";
        }
        path += "_texture.png";
        return new ResourceLocation(CloudRider.MODID, path);
    }

    @Override
    public ResourceLocation getAnimationResource(CloudSteedEntity animatable) {
        return null;
    }

    @Override
    public RenderType getRenderType(CloudSteedEntity animatable, ResourceLocation texture) {
        if (this.controlledAndFirstPersonView(animatable)) {
            return RenderType.entityTranslucentEmissive(texture);
        }
        return super.getRenderType(animatable, texture);
    }

    private boolean controlledAndFirstPersonView(CloudSteedEntity animatable) {
        return animatable.isControlledByLocalInstance() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

}
