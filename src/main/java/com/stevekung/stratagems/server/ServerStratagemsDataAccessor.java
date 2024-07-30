package com.stevekung.stratagems.server;

import com.stevekung.stratagems.StratagemsData;

public interface ServerStratagemsDataAccessor
{
    default StratagemsData getServerStratagemData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}