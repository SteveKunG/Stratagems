package com.stevekung.stratagems;

public interface StratagemsDataAccessor
{
    default StratagemsData getStratagemData()
    {
        throw new AssertionError("Implemented via mixin");
    }
}