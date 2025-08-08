package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.stevekung.stratagems.api.ServerStratagemsData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.storage.DimensionDataStorage;

@Mixin(ServerLevel.class)
public class MixinMinecraftServer
{
    @Inject(method = "createLevels", at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.readScoreboard(Lnet/minecraft/world/level/storage/DimensionDataStorage;)V"))
    private void stratagems$createDataStorage(ChunkProgressListener listener, CallbackInfo info, @Local DimensionDataStorage dimensionDataStorage)
    {
        dimensionDataStorage.computeIfAbsent(ServerStratagemsData.TYPE);
    }
}