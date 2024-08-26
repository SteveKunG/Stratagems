package com.stevekung.stratagems.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.stevekung.stratagems.client.NewDeathScreen;
import com.stevekung.stratagems.registry.ModGameRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener
{
    @Shadow
    ClientLevel level;

    @WrapOperation(method = "handlePlayerCombatKill", at = @At(value = "INVOKE", target = "net/minecraft/client/Minecraft.setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void setNewDeathScreen(Minecraft minecraft, Screen guiScreen, Operation<Void> operation, @Local(argsOnly = true) ClientboundPlayerCombatKillPacket packet)
    {
        if (this.level.getGameRules().getRule(ModGameRules.REINFORCE_ON_DEATH).get())
        {
            guiScreen = new NewDeathScreen(packet.message());
        }
        operation.call(minecraft, guiScreen);
    }
}