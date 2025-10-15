package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.stevekung.stratagems.api.ServerStratagemsData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer
{
    @Inject(method = "createLevels", at = @At(value = "INVOKE", target = "net/minecraft/server/ServerScoreboard.load(Lnet/minecraft/world/scores/ScoreboardSaveData$Packed;)V"))
    private void stratagems$createDataStorage(CallbackInfo info, @Local DimensionDataStorage dimensionDataStorage)
    {
        dimensionDataStorage.computeIfAbsent(ServerStratagemsData.TYPE);
    }
}