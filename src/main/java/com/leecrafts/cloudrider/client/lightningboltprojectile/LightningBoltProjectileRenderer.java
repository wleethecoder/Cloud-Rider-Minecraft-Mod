package com.leecrafts.cloudrider.client.lightningboltprojectile;

import com.leecrafts.cloudrider.entity.custom.LightningBoltProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LightningBoltProjectileRenderer extends GeoEntityRenderer<LightningBoltProjectileEntity> {

    public LightningBoltProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LightningBoltProjectileModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, LightningBoltProjectileEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-animatable.getXRot()));
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

}
