package com.leecrafts.cloudrider.client.lightningboltprojectile;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.LightningBoltProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LightningBoltProjectileRenderer extends GeoEntityRenderer<LightningBoltProjectileEntity> {

    public LightningBoltProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LightningBoltProjectileModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LightningBoltProjectileEntity instance) {
        int mod4 = instance.tickCount % 4;
        if (mod4 == 0 || mod4 == 1){
            return new ResourceLocation(CloudRider.MODID, "textures/entity/lightning_bolt_projectile_texture1.png");
        }
        else {
            return new ResourceLocation(CloudRider.MODID, "textures/entity/lightning_bolt_projectile_texture2.png");
        }
    }

    @Override
    public void actuallyRender(PoseStack poseStack, LightningBoltProjectileEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-animatable.getXRot()));
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

}
