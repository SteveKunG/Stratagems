package com.stevekung.stratagems;

import java.util.Map;

import net.minecraft.core.Holder;

public interface PlayerStratagemsAccessor
{
    default Map<Holder<Stratagem>, StratagemInstance> getPlayerStratagems()
    {
        throw new AssertionError("Implemented via mixin");
    }
}