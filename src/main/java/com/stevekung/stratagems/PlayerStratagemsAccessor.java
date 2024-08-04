package com.stevekung.stratagems;

import java.util.Map;

import net.minecraft.core.Holder;

public interface PlayerStratagemsAccessor
{
    default Map<Holder<Stratagem>, StratagemInstance> getStratagems()
    {
        throw new AssertionError("Implemented via mixin");
    }
}