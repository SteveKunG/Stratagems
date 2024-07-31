package com.stevekung.stratagems.rule;

import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;
import net.minecraft.world.entity.player.Player;

public interface StratagemRule
{
    StratagemRuleType getType();

    boolean canUse(StratagemInstance instance, Player player);

    void onUse(StratagemInstance instance, Player player);

    default void onReset(StratagemInstance instance, Player player)
    {
        instance.state = StratagemState.READY;
        instance.resetStratagemTicks(instance.stratagem().properties());
    }

    void tick(StratagemInstance instance, Player player);

    @FunctionalInterface
    interface Builder
    {
        StratagemRule build();
    }
}