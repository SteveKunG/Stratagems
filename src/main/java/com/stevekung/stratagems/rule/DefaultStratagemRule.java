package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemInstanceContext;
import com.stevekung.stratagems.StratagemState;
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
    public boolean canUse(StratagemInstanceContext context)
    {
        return context.instance().isReady();
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        // Set state from READY to IN_USE
        context.instance().state = StratagemState.IN_USE;
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var stratagem = instance.stratagem();

        if (!instance.isReady())
        {
            if (instance.state == StratagemState.IN_USE)
            {
                if (instance.duration != null && instance.duration > 0)
                {
                    instance.duration--;

                    if (instance.duration % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has duration: {}", stratagem.name().getString(), instance.formatTickDuration(instance.duration, player));
                    }
                }
                else
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", stratagem.name().getString(), instance.state, StratagemState.INBOUND);
                    instance.state = StratagemState.INBOUND;
                }
            }

            if (instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
            {
                instance.inboundDuration--;

                if (instance.inboundDuration % 20 == 0)
                {
                    LOGGER.info("{} stratagem has inboundDuration: {}", stratagem.name().getString(), instance.formatTickDuration(instance.inboundDuration, player));
                }
            }

            if (instance.state != StratagemState.COOLDOWN && instance.inboundDuration == 0)
            {
                LOGGER.info("{} stratagem switch state from {} to {}", stratagem.name().getString(), instance.state, StratagemState.COOLDOWN);
                instance.state = StratagemState.COOLDOWN;
                instance.cooldown = stratagem.properties().cooldown();
            }

            if (instance.state == StratagemState.COOLDOWN)
            {
                if (instance.cooldown > 0)
                {
                    instance.cooldown--;

                    if (instance.cooldown % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has cooldown: {}", stratagem.name().getString(), instance.formatTickDuration(instance.cooldown, player));
                    }
                }

                if (instance.cooldown == 0)
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", stratagem.name().getString(), instance.state, StratagemState.READY);
                    instance.state = StratagemState.READY;
                    instance.resetStratagemTicks(stratagem.properties());
                }
            }
        }
    }

    public static StratagemRule.Builder defaultRule()
    {
        return DefaultStratagemRule::new;
    }
}