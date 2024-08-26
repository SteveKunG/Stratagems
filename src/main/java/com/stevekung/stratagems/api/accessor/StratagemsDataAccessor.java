package com.stevekung.stratagems.api.accessor;

import com.stevekung.stratagems.api.StratagemsData;

/**
 * Used to accessing stratagem data on the specific side, player or server.
 */
public interface StratagemsDataAccessor
{
    default StratagemsData stratagemsData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}