package com.leecrafts.cloudrider.client.cloudsteed;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CloudSteedRenderer extends GeoEntityRenderer<CloudSteedEntity> {

    // TODO transparent when riding?
    private static final ResourceLocation WHITE_CLOUD_STEED_TEXUTRE = new ResourceLocation(CloudRider.MODID, "textures/entity/white_cloud_steed_texture.png");
    private static final ResourceLocation GRAY_CLOUD_STEED_TEXUTRE = new ResourceLocation(CloudRider.MODID, "textures/entity/gray_cloud_steed_texture.png");

    public CloudSteedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CloudSteedModel());
        this.shadowRadius = 0.75f;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CloudSteedEntity animatable) {
        if (animatable.getVariant() == CloudSteedEntity.Type.WHITE) {
            return WHITE_CLOUD_STEED_TEXUTRE;
        }
        else {
            return GRAY_CLOUD_STEED_TEXUTRE;
        }
    }

    @Override
    public void actuallyRender(PoseStack poseStack, CloudSteedEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot())));
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
