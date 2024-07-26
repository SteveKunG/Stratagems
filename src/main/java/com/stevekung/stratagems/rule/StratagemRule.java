package com.stevekung.stratagems.rule;

import com.stevekung.stratagems.StratagemEntry;

public interface StratagemRule
{
    StratagemRuleType getType();

    boolean canUse(StratagemEntry entry);

    void onUse(StratagemEntry entry);

    void tick(StratagemEntry entry);

    @FunctionalInterface
    interface Builder
    {
        StratagemRule build();
    }
}