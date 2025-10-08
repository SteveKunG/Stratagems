package com.stevekung.stratagems.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stevekung.stratagems.client.renderer.state.StratagemPodEntityRenderState;
import com.stevekung.stratagems.entity.StratagemPod;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;

public class StratagemPodRenderer extends EntityRenderer<StratagemPod, StratagemPodEntityRenderState>
{
    public StratagemPodRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    protected boolean affectedByCulling(StratagemPod entity)
    {
        return false;
    }

    @Override
    public StratagemPodEntityRenderState createRenderState()
    {
        return new StratagemPodEntityRenderState();
    }

    @Override
    public void extractRenderState(StratagemPod entity, StratagemPodEntityRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        state.beamColor = entity.getVariant().value().properties().beamColor();
    }

    @Override
    public void submit(StratagemPodEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState)
    {
        var pos = BlockPos.containing(renderState.x, renderState.y, renderState.z);
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);

        //Minecraft.getInstance().levelRenderer.cullingFrustum.isVisible(new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, Math.min(pos.getY(), 1024) + 1, pos.getZ() + 1))TODO
        {
            BeaconRenderer.submitBeaconBeam(poseStack, nodeCollector, BeaconRenderer.BEAM_LOCATION, renderState.ageInTicks, 1.0f, 0, 1024, renderState.beamColor, 0.1F, 0.25F);
        }
        poseStack.popPose();
    }
}