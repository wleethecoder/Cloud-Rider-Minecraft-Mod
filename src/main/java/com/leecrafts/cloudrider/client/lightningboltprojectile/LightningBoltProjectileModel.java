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
        return new ResourceLocation(CloudRider.MODID, "textures/entity/lightning_bolt_projectile_texture1.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LightningBoltProjectileEntity animatable) {
        return null;
    }

}
