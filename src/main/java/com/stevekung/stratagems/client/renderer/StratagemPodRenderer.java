package com.stevekung.stratagems.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stevekung.stratagems.client.renderer.state.StratagemPodEntityRenderState;
import com.stevekung.stratagems.entity.StratagemPod;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
        state.gameTime = entity.level().getGameTime();
        state.beamColor = entity.getVariant().value().properties().beamColor();
    }

    @Override
    public void render(StratagemPodEntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        var pos = BlockPos.containing(state.x, state.y, state.z);
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);

        //Minecraft.getInstance().levelRenderer.cullingFrustum.isVisible(new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, Math.min(pos.getY(), 1024) + 1, pos.getZ() + 1))TODO
        {
            BeaconRenderer.renderBeaconBeam(poseStack, buffer, BeaconRenderer.BEAM_LOCATION, state.ageInTicks, 1.0f, state.gameTime, 0, 1024, state.beamColor, 0.1F, 0.25F);
        }
        poseStack.popPose();
    }
}