package com.stevekung.stratagems.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.StratagemRules;

import net.minecraft.world.entity.player.Player;

public class ReplenishStratagemRule implements StratagemRule
{
    public static final MapCodec<ReplenishStratagemRule> CODEC = MapCodec.unit(new ReplenishStratagemRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.REPLENISH;
    }

    @Override
    public boolean canUse(StratagemEntry entry)
    {
        return entry.isReady();
    }

    @Override
    public void onUse(StratagemEntry entry, Player player)
    {
        var stratagemData = entry.level().getStratagemData();
        var rearmProperties = entry.stratagem().properties();
        var count = 0;

        if (rearmProperties.replenish().isPresent() && rearmProperties.replenish().get().toReplenish().isPresent())
        {
            for (var toReplenishEntry : stratagemData.getStratagemEntries().stream().filter(toReplenishEntry -> rearmProperties.replenish().get().toReplenish().get().contains(toReplenishEntry.getStratagem())).toList())
            {
                toReplenishEntry.state = StratagemState.COOLDOWN;

                var replenishedProperties = toReplenishEntry.stratagem().properties();
                toReplenishEntry.inboundDuration = replenishedProperties.inboundDuration();

                if (replenishedProperties.duration().isPresent())
                {
                    toReplenishEntry.duration = replenishedProperties.duration().get();
                }

                // replenished cooldown from rearm
                toReplenishEntry.cooldown = rearmProperties.cooldown();

                if (replenishedProperties.remainingUse().isPresent())
                {
                    toReplenishEntry.remainingUse = replenishedProperties.remainingUse().get();
                }

                LOGGER.info("Replenished {} stratagem!", toReplenishEntry.stratagem().name().getString());

                // Remove this rearm stratagem
                stratagemData.remove(entry.getStratagem());
                LOGGER.info("Remove {} replenisher stratagem!", entry.stratagem().name().getString());
                count++;
            }

            if (rearmProperties.replenish().get().replenishSound().isPresent() && count > 0)
            {
                player.playSound(rearmProperties.replenish().get().replenishSound().get(), 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void onReset(StratagemEntry entry)
    {
        entry.level().getStratagemData().remove(entry.getStratagem());
        LOGGER.info("Remove {} replenisher stratagem on reset!", entry.stratagem().name().getString());
    }

    @Override
    public void tick(StratagemEntry entry)
    {
    }

    public static Builder defaultRule()
    {
        return ReplenishStratagemRule::new;
    }
}