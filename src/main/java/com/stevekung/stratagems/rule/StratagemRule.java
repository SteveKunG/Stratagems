package com.stevekung.stratagems.rule;

import com.stevekung.stratagems.StratagemInstanceContext;
import com.stevekung.stratagems.StratagemState;

public interface StratagemRule
{
    StratagemRuleType getType();

    boolean canUse(StratagemInstanceContext context);

    void onUse(StratagemInstanceContext context);

    default void onReset(StratagemInstanceContext context)
    {
        context.instance().state = StratagemState.READY;
        context.instance().resetStratagemTicks(context.instance().stratagem().properties());
    }

    void tick(StratagemInstanceContext context);

    @FunctionalInterface
    interface Builder
    {
        StratagemRule build();
    }
}