package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.StratagemsTicker;
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
    public boolean canUse(StratagemsTicker ticker)
    {
        if (ticker.remainingUse == 0)
        {
//            LOGGER.info("Cannot use {} stratagem!", ticker.stratagem().name().getString());
            return false;
        }
        return ticker.remainingUse > 0;
    }

    @Override
    public void onUse(StratagemsTicker ticker)
    {
        System.out.println(ticker.remainingUse);
        if (ticker.remainingUse != null && ticker.remainingUse == 0)
        {
            ticker.state = StratagemState.DEPLETED;
            LOGGER.info("Cannot use {} stratagem!", ticker.stratagem().name().getString());
            return;
        }
        if (ticker.remainingUse != null && ticker.remainingUse > 0)
        {
            // Set state from READY to IN_USE
            ticker.state = StratagemState.IN_USE;
            ticker.remainingUse--;
            LOGGER.info("{} stratagem has remainingUse: {}", ticker.stratagem().name().getString(), ticker.remainingUse);
        }
    }

    @Override
    public void tick(StratagemsTicker ticker)
    {
        if (!ticker.isReady())
        {
            if (ticker.state == StratagemState.IN_USE)
            {
                if (ticker.duration != null && ticker.duration > 0)
                {
                    ticker.duration--;

                    if (ticker.duration % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has duration: {}", ticker.stratagem().name().getString(), ticker.formatTickDuration(ticker.duration));
                    }
                }
                else
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", ticker.stratagem().name().getString(), ticker.state, StratagemState.INCOMING);
                    ticker.state = StratagemState.INCOMING;
                }
            }

            if (ticker.state == StratagemState.INCOMING && ticker.incomingDuration > 0)
            {
                ticker.incomingDuration--;

                if (ticker.incomingDuration % 20 == 0)
                {
                    LOGGER.info("{} stratagem has incomingDuration: {}", ticker.stratagem().name().getString(), ticker.formatTickDuration(ticker.incomingDuration));
                }
            }

            if (ticker.state != StratagemState.COOLDOWN && ticker.incomingDuration == 0)
            {
                LOGGER.info("{} stratagem switch state from {} to {}", ticker.stratagem().name().getString(), ticker.state, StratagemState.COOLDOWN);
                ticker.state = StratagemState.COOLDOWN;
                ticker.cooldown = ticker.stratagem().properties().cooldown();
            }

            if (ticker.state == StratagemState.COOLDOWN)
            {
                if (ticker.cooldown > 0)
                {
                    ticker.cooldown--;

                    if (ticker.cooldown % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has cooldown: {}", ticker.stratagem().name().getString(), ticker.formatTickDuration(ticker.cooldown));
                    }
                }

                if (ticker.cooldown == 0)
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", ticker.stratagem().name().getString(), ticker.state, StratagemState.READY);
                    ticker.state = StratagemState.READY;
                    var properties = ticker.stratagem().properties();
                    ticker.incomingDuration = properties.incomingDuration();

                    if (properties.duration().isPresent())
                    {
                        ticker.duration = properties.duration().get();
                    }

                    ticker.cooldown = properties.cooldown();
                }
            }
        }
    }

    public static Builder defaultRule()
    {
        return DepletedStratagemRule::new;
    }
}