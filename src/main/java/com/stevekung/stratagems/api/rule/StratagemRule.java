package com.stevekung.stratagems.api.rule;

import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemModifier;
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
     * Called when being blocked or use by command
     *
     * @param context this stratagem context
     * @param unblock unblock flag
     */
    default void onBlocked(StratagemInstanceContext context, boolean unblock)
    {
        if (unblock)
        {
            context.instance().state = StratagemState.READY;
        }
        else
        {
            context.instance().state = StratagemState.BLOCKED;
        }
    }

    /**
     * Called when being set or clear modifier by command
     *
     * @param context this stratagem context
     * @param modifier stratagem modifier to apply
     * @param clear clear a stratagem modifier flag
     */
    default void onModified(StratagemInstanceContext context, StratagemModifier modifier, boolean clear)
    {
        switch (modifier)
        {
            case RANDOMIZE:
                context.instance().modifier = clear ? StratagemModifier.NONE : StratagemModifier.RANDOMIZE;
                break;
            default:
                break;
        }
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