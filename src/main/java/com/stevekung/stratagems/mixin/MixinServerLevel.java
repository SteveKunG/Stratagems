package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.stevekung.stratagems.StratagemsData;
import com.stevekung.stratagems.server.ServerStratagemsDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements ServerStratagemsDataAccessor
{
    @Shadow
    abstract MinecraftServer getServer();

    @Override
    public StratagemsData getServerStratagemData()
    {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(StratagemsData.factory(ServerLevel.class.cast(this)), StratagemsData.getFileId());
    }
}