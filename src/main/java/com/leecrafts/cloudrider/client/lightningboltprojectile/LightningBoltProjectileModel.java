package com.leecrafts.cloudrider.client.lightningboltprojectile;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.LightningBoltProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LightningBoltProjectileModel extends GeoModel<LightningBoltProjectileEntity> {

    @Override
    public ResourceLocation getModelResource(LightningBoltProjectileEntity animatable) {
        return new ResourceLocation(CloudRider.MODID, "geo/lightning_bolt_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LightningBoltProjectileEntity animatable) {
        String path = "textures/entity/lightning_bolt_projectile_texture";
        if (animatable.tickCount % 4 <= 1) {
            path += "1.png";
        }
        else {
            path += "2.png";
        }
        return new ResourceLocation(CloudRider.MODID, path);
    }

    @Override
    public ResourceLocation getAnimationResource(LightningBoltProjectileEntity animatable) {
        return null;
    }

}
