package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.StratagemUtils;
import com.stevekung.stratagems.registry.ModRegistries;
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
        return entry.isReady() && entry.remainingUse > 0;
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
                if (entry.inboundDuration == 0)
                {
                    if (entry.remainingUse == 0)
                    {
                        entry.state = StratagemState.DEPLETED;
                        LOGGER.info("{} stratagem is now depleted!", entry.stratagem().name().getString());

                        // Add replenisher stratagem when remaining use is 0
                        if (entry.stratagem().properties().replenish().isPresent() && entry.stratagem().properties().replenish().get().replenisher().isPresent())
                        {
                            var replenisher = entry.level().registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(entry.stratagem().properties().replenish().get().replenisher().get());
                            entry.level().getStratagemData().add(StratagemUtils.createCompoundTagWithDefaultValue(replenisher));
                            LOGGER.info("Add {} replenisher stratagem", replenisher.value().name().getString());
                        }
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
                    entry.inboundDuration = properties.inboundDuration();

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