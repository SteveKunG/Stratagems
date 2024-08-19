package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.stevekung.stratagems.api.ServerStratagemsData;
import com.stevekung.stratagems.api.accessor.StratagemsDataAccessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements StratagemsDataAccessor
{
    @Shadow
    abstract MinecraftServer getServer();

    @Override
    public ServerStratagemsData stratagemsData()
    {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(ServerStratagemsData.factory(ServerLevel.class.cast(this)), ServerStratagemsData.getFileId());
    }
}