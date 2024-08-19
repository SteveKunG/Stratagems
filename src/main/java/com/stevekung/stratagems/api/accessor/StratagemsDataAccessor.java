package com.stevekung.stratagems.api.accessor;

import com.stevekung.stratagems.api.StratagemsData;

public interface StratagemsDataAccessor
{
    default StratagemsData stratagemsData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}