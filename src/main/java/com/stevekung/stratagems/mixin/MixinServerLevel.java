package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.stevekung.stratagems.StratagemsData;
import com.stevekung.stratagems.StratagemsDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements StratagemsDataAccessor
{
    @Shadow
    abstract MinecraftServer getServer();

    @Override
    public StratagemsData getStratagemData()
    {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(StratagemsData.factory(ServerLevel.class.cast(this)), StratagemsData.getFileId());
    }
}