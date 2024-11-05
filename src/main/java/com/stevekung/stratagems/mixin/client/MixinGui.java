package com.stevekung.stratagems.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stevekung.stratagems.client.StratagemsClientMod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(Gui.class)
public class MixinGui
{
    @Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
    private void stratagems$renderStratagemHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info)
    {
        StratagemsClientMod.renderHud(guiGraphics, deltaTracker);
    }
}