package com.stevekung.stratagems.api.accessor;

import com.stevekung.stratagems.api.ServerStratagemsData;

public interface ServerStratagemsDataAccessor
{
    default ServerStratagemsData stratagemsData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}