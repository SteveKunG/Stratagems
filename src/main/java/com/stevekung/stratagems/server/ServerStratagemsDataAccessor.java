package com.stevekung.stratagems.server;

import com.stevekung.stratagems.ServerStratagemsData;

public interface ServerStratagemsDataAccessor
{
    default ServerStratagemsData getServerStratagemData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}