package com.leecrafts.cloudrider.client.lightningboltprojectile;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.LightningBoltProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LightningBoltProjectileRenderer extends GeoEntityRenderer<LightningBoltProjectileEntity> {

    public LightningBoltProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LightningBoltProjectileModel());
        this.shadowRadius = 0.0f;
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull LightningBoltProjectileEntity instance) {
        return new ResourceLocation(CloudRider.MODID, "textures/entity/lightning_bolt_projectile_texture.png");
    }

}
