package com.stevekung.stratagems.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import com.stevekung.stratagems.client.NewDeathScreen;
import com.stevekung.stratagems.registry.ModGameRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Shadow
    ClientLevel level;

    @ModifyVariable(method = "setScreen", at = @At("STORE"), argsOnly = true, slice = @Slice(from = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.shouldShowDeathScreen()Z")))
    private Screen setNewDeathScreen(@Nullable Screen guiScreen)
    {
        if (this.level.getGameRules().getRule(ModGameRules.REINFORCE_ON_DEATH).get())
        {
            return new NewDeathScreen(null);
        }
        return guiScreen;
    }
}