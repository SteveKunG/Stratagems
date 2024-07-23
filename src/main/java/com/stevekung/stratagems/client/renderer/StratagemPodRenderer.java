package com.stevekung.stratagems.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stevekung.stratagems.entity.StratagemPod;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class StratagemPodRenderer extends EntityRenderer<StratagemPod>
{
    public StratagemPodRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(StratagemPod entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        var gameTime = entity.level().getGameTime();
        var pos = entity.blockPosition();
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);

        //Minecraft.getInstance().levelRenderer.cullingFrustum.isVisible(new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, Math.min(pos.getY(), 1024) + 1, pos.getZ() + 1))TODO
        {
            BeaconRenderer.renderBeaconBeam(poseStack, buffer, BeaconRenderer.BEAM_LOCATION, partialTicks, 1.0f, gameTime, 0, 1024, entity.getVariant().value().properties().beamColor(), 0.1F, 0.25F);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(StratagemPod entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
