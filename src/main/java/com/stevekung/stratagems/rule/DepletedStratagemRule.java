package com.stevekung.stratagems.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemInstance.Side;
import com.stevekung.stratagems.StratagemInstanceContext;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.StratagemRules;
import com.stevekung.stratagems.util.StratagemUtils;

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
    public boolean canUse(StratagemInstanceContext context)
    {
        return context.instance().isReady() && context.instance().remainingUse > 0;
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        var instance = context.instance();

        if (instance.remainingUse > 0)
        {
            // Set state from READY to IN_USE
            instance.state = StratagemState.IN_USE;
            instance.remainingUse--;
            LOGGER.info("{} stratagem has remainingUse: {}", instance.stratagem().name().getString(), instance.remainingUse);
        }

        // Add replenisher stratagem when remaining use is 0
        if (instance.remainingUse < instance.stratagem().properties().remainingUse().get() && instance.stratagem().properties().replenish().isPresent() && instance.stratagem().properties().replenish().get().replenisher().isPresent())
        {
            if (instance.side == Side.PLAYER && context.player().isPresent())
            {
                var replenisher = context.player().get().level().registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(instance.stratagem().properties().replenish().get().replenisher().get());

                if (StratagemUtils.anyMatchHolder(context.player().get().getPlayerStratagems().values(), replenisher))
                {
                    LOGGER.info("{} player replenisher stratagem already exist", replenisher.value().name().getString());
                    return;
                }

                context.player().get().getPlayerStratagems().put(replenisher, StratagemUtils.createInstanceWithDefaultValue(replenisher, StratagemInstance.Side.PLAYER));
                LOGGER.info("Add {} replenisher stratagem to {}", replenisher.value().name().getString(), context.player().get().getName().getString());
            }
            if (instance.side == Side.SERVER && context.minecraftServer().isPresent())
            {
                var replenisher = context.minecraftServer().get().registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(instance.stratagem().properties().replenish().get().replenisher().get());

                if (StratagemUtils.anyMatchHolder(context.minecraftServer().get().overworld().getServerStratagemData().getStratagemInstances(), replenisher))
                {
                    LOGGER.info("{} server replenisher stratagem already exist", replenisher.value().name().getString());
                    return;
                }

                context.minecraftServer().get().overworld().getServerStratagemData().add(StratagemUtils.createInstanceWithDefaultValue(replenisher, StratagemInstance.Side.SERVER));
                LOGGER.info("Add {} server replenisher stratagem", replenisher.value().name().getString());
            }
        }
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player().orElse(null);

        if (!instance.isReady())
        {
            if (instance.state == StratagemState.IN_USE)
            {
                if (instance.duration != null && instance.duration > 0)
                {
                    instance.duration--;

                    if (instance.duration % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has duration: {}", instance.stratagem().name().getString(), instance.formatTickDuration(instance.duration, player));
                    }
                }
                else
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", instance.stratagem().name().getString(), instance.state, StratagemState.INBOUND);
                    instance.state = StratagemState.INBOUND;
                }
            }

            if (instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
            {
                instance.inboundDuration--;

                if (instance.inboundDuration % 20 == 0)
                {
                    LOGGER.info("{} stratagem has inboundDuration: {}", instance.stratagem().name().getString(), instance.formatTickDuration(instance.inboundDuration, player));
                }
                if (instance.inboundDuration == 0)
                {
                    if (instance.remainingUse == 0)
                    {
                        instance.state = StratagemState.DEPLETED;
                        LOGGER.info("{} stratagem is now depleted!", instance.stratagem().name().getString());
                        return;
                    }
                    LOGGER.info("{} stratagem switch state from {} to {}", instance.stratagem().name().getString(), instance.state, StratagemState.COOLDOWN);
                    instance.state = StratagemState.COOLDOWN;
                    instance.cooldown = instance.stratagem().properties().cooldown();
                }
            }

            if (instance.state == StratagemState.COOLDOWN)
            {
                if (instance.cooldown > 0)
                {
                    instance.cooldown--;

                    if (instance.cooldown % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has cooldown: {}", instance.stratagem().name().getString(), instance.formatTickDuration(instance.cooldown, player));
                    }
                }

                if (instance.cooldown == 0)
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", instance.stratagem().name().getString(), instance.state, StratagemState.READY);
                    instance.state = StratagemState.READY;

                    var properties = instance.stratagem().properties();
                    instance.inboundDuration = properties.inboundDuration();

                    if (properties.duration().isPresent())
                    {
                        instance.duration = properties.duration().get();
                    }

                    instance.cooldown = properties.cooldown();
                }
            }
        }
    }

    public static Builder defaultRule()
    {
        return DepletedStratagemRule::new;
    }
}