package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.stevekung.stratagems.ServerStratagemsData;
import com.stevekung.stratagems.server.ServerStratagemsDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements ServerStratagemsDataAccessor
{
    @Shadow
    abstract MinecraftServer getServer();

    @Override
    public ServerStratagemsData getStratagemData()
    {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(ServerStratagemsData.factory(ServerLevel.class.cast(this)), ServerStratagemsData.getFileId());
    }
}