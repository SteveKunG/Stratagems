package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemsTicker;
import com.stevekung.stratagems.registry.StratagemRules;
import net.minecraft.util.StringUtil;

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
            LOGGER.info("Cannot use this stratagem! {}", ticker.getStratagem().getRegisteredName());
        }
        return ticker.remainingUse > 0;
    }

    @Override
    public void onUse(StratagemsTicker ticker)
    {
        if (ticker.remainingUse > 0)
        {
            ticker.remainingUse--;
            LOGGER.info("{}, remainingUse:{}", ticker.getStratagem().getRegisteredName(), ticker.remainingUse);
        }
    }

    @Override
    public void tick(StratagemsTicker ticker)
    {
        if (ticker.state != StratagemsTicker.State.COOLDOWN && ticker.remainingUse == 0)
        {
            ticker.nextUseCooldown = ticker.stratagem().properties().nextUseCooldown();
            ticker.state = StratagemsTicker.State.COOLDOWN;
            LOGGER.info("{}, no remaining use, state:{}, nextUseCooldown:{}", ticker.getStratagem().getRegisteredName(), ticker.state, StringUtil.formatTickDuration(ticker.nextUseCooldown, ticker.level.tickRateManager().tickrate()));
        }

        if (ticker.state == StratagemsTicker.State.COOLDOWN)
        {
            if (ticker.nextUseCooldown > 0)
            {
                ticker.nextUseCooldown--;

                if (ticker.nextUseCooldown % 20 == 0)
                {
                    LOGGER.info("{}, state:{}, nextUseCooldown:{}", ticker.getStratagem().getRegisteredName(), ticker.state, StringUtil.formatTickDuration(ticker.nextUseCooldown, ticker.level.tickRateManager().tickrate()));
                }
            }

            if (ticker.nextUseCooldown == 0)
            {
                ticker.state = StratagemsTicker.State.READY;
                ticker.remainingUse++;
                LOGGER.info("{}, switch to state:{}, remainingUse:{}", ticker.getStratagem().getRegisteredName(), ticker.state, ticker.remainingUse);
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReinforceStratagemRule::new;
    }
}