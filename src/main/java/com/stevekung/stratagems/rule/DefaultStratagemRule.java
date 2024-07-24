package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemsTicker;
import com.stevekung.stratagems.registry.StratagemRules;
import net.minecraft.util.StringUtil;

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
    public boolean canUse(StratagemsTicker ticker)
    {
        return ticker.isReady();
    }

    @Override
    public void onUse(StratagemsTicker ticker)
    {
        if (ticker.remainingUse == 0)
        {
            LOGGER.info("Cannot use this stratagem! {}", ticker.getStratagem().getRegisteredName());
            return;
        }

        ticker.state = StratagemsTicker.State.IN_USE;

        if (ticker.remainingUse > 0)
        {
            ticker.remainingUse--;
            LOGGER.info("{}, remainingUse:{}", ticker.getStratagem().getRegisteredName(), ticker.remainingUse);
        }
    }

    @Override
    public void tick(StratagemsTicker ticker)
    {
        if (!ticker.isReady())
        {
            if (ticker.state == StratagemsTicker.State.IN_USE)
            {
                if (ticker.duration > 0)
                {
                    ticker.duration--;

                    if (ticker.duration % 20 == 0)
                    {
                        LOGGER.info("{}, duration:{}", ticker.getStratagem().getRegisteredName(), StringUtil.formatTickDuration(ticker.duration, ticker.level.tickRateManager().tickrate()));
                    }
                }
                else
                {
                    ticker.state = StratagemsTicker.State.INCOMING;
                    LOGGER.info("{}, switch to state:{}", ticker.getStratagem().getRegisteredName(), ticker.state);
                }
            }

            if (ticker.state == StratagemsTicker.State.INCOMING && ticker.incomingDuration > 0)
            {
                ticker.incomingDuration--;

                if (ticker.incomingDuration % 20 == 0)
                {
                    LOGGER.info("{}, incomingDuration:{}", ticker.getStratagem().getRegisteredName(), StringUtil.formatTickDuration(ticker.incomingDuration, ticker.level.tickRateManager().tickrate()));
                }
            }

            if (ticker.state != StratagemsTicker.State.COOLDOWN && ticker.incomingDuration == 0)
            {
                ticker.state = StratagemsTicker.State.COOLDOWN;
                ticker.nextUseCooldown = ticker.stratagem().properties().nextUseCooldown();
                LOGGER.info("{}, switch to state:{}", ticker.getStratagem().getRegisteredName(), ticker.state);
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
                    ticker.setDefaultStratagemTicks(ticker.stratagem().properties());
                    LOGGER.info("{}, switch to state:{}", ticker.getStratagem().getRegisteredName(), ticker.state);
                }
            }
        }
    }

    public static StratagemRule.Builder defaultRule()
    {
        return DefaultStratagemRule::new;
    }
}