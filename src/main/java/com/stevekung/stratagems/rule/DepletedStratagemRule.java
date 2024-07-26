package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.registry.StratagemRules;

public class DepletedStratagemRule implements StratagemRule
{
    public static final MapCodec<DepletedStratagemRule> CODEC = MapCodec.unit(new DepletedStratagemRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.DEPLETED;
    }

    @Override
    public boolean canUse(StratagemEntry entry)
    {
        return entry.remainingUse > 0;
    }

    @Override
    public void onUse(StratagemEntry entry)
    {
        if (entry.remainingUse > 0)
        {
            // Set state from READY to IN_USE
            entry.state = StratagemState.IN_USE;
            entry.remainingUse--;
            LOGGER.info("{} stratagem has remainingUse: {}", entry.stratagem().name().getString(), entry.remainingUse);
        }
    }

    @Override
    public void tick(StratagemEntry entry)
    {
        if (!entry.isReady())
        {
            if (entry.state == StratagemState.IN_USE)
            {
                if (entry.duration != null && entry.duration > 0)
                {
                    entry.duration--;

                    if (entry.duration % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has duration: {}", entry.stratagem().name().getString(), entry.formatTickDuration(entry.duration));
                    }
                }
                else
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", entry.stratagem().name().getString(), entry.state, StratagemState.INCOMING);
                    entry.state = StratagemState.INCOMING;
                }
            }

            if (entry.state == StratagemState.INCOMING && entry.incomingDuration > 0)
            {
                entry.incomingDuration--;

                if (entry.incomingDuration % 20 == 0)
                {
                    LOGGER.info("{} stratagem has incomingDuration: {}", entry.stratagem().name().getString(), entry.formatTickDuration(entry.incomingDuration));
                }
                if (entry.incomingDuration == 0)
                {
                    if (entry.remainingUse == 0)
                    {
                        entry.state = StratagemState.DEPLETED;
                        LOGGER.info("{} stratagem is now depleted!", entry.stratagem().name().getString());
                        return;
                    }
                    LOGGER.info("{} stratagem switch state from {} to {}", entry.stratagem().name().getString(), entry.state, StratagemState.COOLDOWN);
                    entry.state = StratagemState.COOLDOWN;
                    entry.cooldown = entry.stratagem().properties().cooldown();
                }
            }

            if (entry.state == StratagemState.COOLDOWN)
            {
                if (entry.cooldown > 0)
                {
                    entry.cooldown--;

                    if (entry.cooldown % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has cooldown: {}", entry.stratagem().name().getString(), entry.formatTickDuration(entry.cooldown));
                    }
                }

                if (entry.cooldown == 0)
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", entry.stratagem().name().getString(), entry.state, StratagemState.READY);
                    entry.state = StratagemState.READY;

                    var properties = entry.stratagem().properties();
                    entry.incomingDuration = properties.incomingDuration();

                    if (properties.duration().isPresent())
                    {
                        entry.duration = properties.duration().get();
                    }

                    entry.cooldown = properties.cooldown();
                }
            }
        }
    }

    public static Builder defaultRule()
    {
        return DepletedStratagemRule::new;
    }
}