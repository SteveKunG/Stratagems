package com.stevekung.stratagems.rule;

import com.stevekung.stratagems.StratagemsTicker;

public interface StratagemRule
{
    StratagemRuleType getType();

    boolean canUse(StratagemsTicker ticker);

    void onUse(StratagemsTicker ticker);

    void tick(StratagemsTicker ticker);

    @FunctionalInterface
    interface Builder
    {
        StratagemRule build();
    }
}