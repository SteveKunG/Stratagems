package com.stevekung.stratagems.api.accessor;

import java.util.Map;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;

import net.minecraft.core.Holder;

public interface PlayerStratagemsAccessor
{
    default Map<Holder<Stratagem>, StratagemInstance> getStratagems()
    {
        throw new AssertionError("Implemented via mixin");
    }

    default int getUniqueStratagemId()
    {
        throw new AssertionError("Implemented via mixin");
    }
}