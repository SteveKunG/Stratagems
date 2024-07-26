package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.registry.StratagemRules;

public class DefaultStratagemRule implements StratagemRule
{
    public static final MapCodec<DefaultStratagemRule> CODEC = MapCodec.unit(new DefaultStratagemRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.DEFAULT;
    }

    @Override
    public boolean canUse(StratagemEntry entry)
    {
        return entry.isReady();
    }

    @Override
    public void onUse(StratagemEntry entry)
    {
        // Set state from READY to IN_USE
        entry.state = StratagemState.IN_USE;
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
                    LOGGER.info("{} stratagem switch state from {} to {}", entry.stratagem().name().getString(), entry.state, StratagemState.INBOUND);
                    entry.state = StratagemState.INBOUND;
                }
            }

            if (entry.state == StratagemState.INBOUND && entry.inboundDuration > 0)
            {
                entry.inboundDuration--;

                if (entry.inboundDuration % 20 == 0)
                {
                    LOGGER.info("{} stratagem has inboundDuration: {}", entry.stratagem().name().getString(), entry.formatTickDuration(entry.inboundDuration));
                }
            }

            if (entry.state != StratagemState.COOLDOWN && entry.inboundDuration == 0)
            {
                LOGGER.info("{} stratagem switch state from {} to {}", entry.stratagem().name().getString(), entry.state, StratagemState.COOLDOWN);
                entry.state = StratagemState.COOLDOWN;
                entry.cooldown = entry.stratagem().properties().cooldown();
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
                    entry.resetStratagemTicks(entry.stratagem().properties());
                }
            }
        }
    }

    public static StratagemRule.Builder defaultRule()
    {
        return DefaultStratagemRule::new;
    }
}