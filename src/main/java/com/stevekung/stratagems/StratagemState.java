package com.stevekung.stratagems;

import java.util.Locale;

public enum StratagemState
{
    READY,
    IN_USE,
    INBOUND,
    COOLDOWN,
    IMPACT,
    DEPLETED,
    BLOCKED;

    private static final StratagemState[] VALUES = values();

    public static StratagemState byName(String name)
    {
        for (var state : VALUES)
        {
            if (name.equalsIgnoreCase(state.name()))
            {
                return state;
            }
        }
        return READY;
    }

    public String getName()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}