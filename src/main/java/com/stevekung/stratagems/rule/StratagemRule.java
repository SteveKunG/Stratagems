package com.stevekung.stratagems.rule;

import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.StratagemState;

import net.minecraft.world.entity.player.Player;

public interface StratagemRule
{
    StratagemRuleType getType();

    boolean canUse(StratagemEntry entry);

    void onUse(StratagemEntry entry, Player player);

    default void onReset(StratagemEntry entry)
    {
        entry.state = StratagemState.READY;
        entry.resetStratagemTicks(entry.stratagem().properties());
    }

    void tick(StratagemEntry entry);

    @FunctionalInterface
    interface Builder
    {
        StratagemRule build();
    }
}