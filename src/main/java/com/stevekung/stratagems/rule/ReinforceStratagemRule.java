package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.StratagemsTicker;
import com.stevekung.stratagems.registry.StratagemRules;

public class ReinforceStratagemRule implements StratagemRule
{
    public static final MapCodec<ReinforceStratagemRule> CODEC = MapCodec.unit(new ReinforceStratagemRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.REINFORCE;
    }

    @Override
    public boolean canUse(StratagemsTicker ticker)
    {
        if (ticker.remainingUse == 0)
        {
            LOGGER.info("Cannot use {} stratagem!", ticker.stratagem().name().getString());
        }
        return ticker.remainingUse > 0;
    }

    @Override
    public void onUse(StratagemsTicker ticker)
    {
        if (ticker.remainingUse > 0)
        {
            LOGGER.info("{} stratagem has remainingUse: {}", ticker.stratagem().name().getString(), ticker.remainingUse);
            ticker.remainingUse--;
        }
    }

    @Override
    public void tick(StratagemsTicker ticker)
    {
        if (ticker.state != StratagemState.COOLDOWN && ticker.remainingUse == 0)
        {
            ticker.cooldown = ticker.stratagem().properties().cooldown();
            ticker.state = StratagemState.COOLDOWN;
            LOGGER.info("{} stratagem has no remaining use, cooldown: {}", ticker.stratagem().name().getString(), ticker.formatTickDuration(ticker.cooldown));
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
                LOGGER.info("{} stratagem switch state from {} to {} with remainingUse: {}", ticker.stratagem().name().getString(), ticker.state, StratagemState.READY, ticker.remainingUse);
                ticker.state = StratagemState.READY;
                ticker.remainingUse++;
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReinforceStratagemRule::new;
    }
}