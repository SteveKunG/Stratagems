package com.stevekung.stratagems.api.accessor;

import com.stevekung.stratagems.api.PlayerStratagemsData;

public interface PlayerStratagemsAccessor
{
    default PlayerStratagemsData stratagemsData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}