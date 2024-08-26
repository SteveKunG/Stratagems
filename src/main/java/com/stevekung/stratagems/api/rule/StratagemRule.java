package com.stevekung.stratagems.api.rule;

import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;

public interface StratagemRule
{
    /**
     * @return type of stratagem rule
     */
    StratagemRuleType getType();

    /**
     * Check if this stratagem can be use or not
     *
     * @param context this stratagem context
     * @return if this stratagem can use
     */
    boolean canUse(StratagemInstanceContext context);

    /**
     * Called when a stratagem is touched the ground or use by command
     *
     * @param context this stratagem context
     */
    void onUse(StratagemInstanceContext context);

    /**
     * Called when cooldown ticking is finish or use by command
     *
     * @param context this stratagem context
     */
    default void onReset(StratagemInstanceContext context)
    {
        context.instance().state = StratagemState.READY;
        context.instance().resetStratagemTicks(context.instance().stratagem().properties());
    }

    /**
     * Called from both server and client ticking
     *
     * @param context this stratagem context
     */
    void tick(StratagemInstanceContext context);

    @FunctionalInterface
    interface Builder
    {
        StratagemRule build();
    }
}