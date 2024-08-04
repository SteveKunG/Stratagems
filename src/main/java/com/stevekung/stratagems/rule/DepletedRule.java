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

public class DepletedRule implements StratagemRule
{
    public static final MapCodec<DepletedRule> CODEC = MapCodec.unit(new DepletedRule());
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
        var player = context.player();
        var server = context.server();
        var stratagem = instance.stratagem();
        var replenishOptional = stratagem.properties().replenish();

        if (instance.remainingUse > 0)
        {
            // Set state from READY to IN_USE
            instance.state = StratagemState.IN_USE;
            instance.remainingUse--;
            LOGGER.info("{} stratagem has remainingUse: {}", stratagem.name().getString(), instance.remainingUse);
        }

        // Add replenisher stratagem when remaining use is lower than original
        if (instance.remainingUse < stratagem.properties().remainingUse().get() && replenishOptional.isPresent())
        {
            var replenisherOptional = replenishOptional.get().replenisher();

            if (replenisherOptional.isPresent())
            {
                var replenisherKey = replenisherOptional.get();

                if (instance.side == Side.PLAYER && player != null)
                {
                    var playerStratagems = player.getStratagems();
                    var replenisherStratagem = player.level().registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(replenisherKey);

                    if (StratagemUtils.anyMatchHolder(playerStratagems.values(), replenisherStratagem))
                    {
                        LOGGER.info("{} player replenisher stratagem already exist", replenisherStratagem.value().name().getString());
                        return;
                    }

                    playerStratagems.put(replenisherStratagem, StratagemUtils.createInstanceWithDefaultValue(replenisherStratagem, StratagemInstance.Side.PLAYER));
                    LOGGER.info("Add {} replenisher stratagem to {}", replenisherStratagem.value().name().getString(), player.getName().getString());
                }
                if (instance.side == Side.SERVER && server != null)
                {
                    var serverStratagems = server.overworld().getStratagemData();
                    var replenisherStratagem = server.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(replenisherKey);

                    if (StratagemUtils.anyMatchHolder(serverStratagems.getInstances(), replenisherStratagem))
                    {
                        LOGGER.info("{} server replenisher stratagem already exist", replenisherStratagem.value().name().getString());
                        return;
                    }

                    serverStratagems.add(StratagemUtils.createInstanceWithDefaultValue(replenisherStratagem, StratagemInstance.Side.SERVER));
                    LOGGER.info("Add {} server replenisher stratagem", replenisherStratagem.value().name().getString());
                }
            }
        }
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var stratagem = instance.stratagem();
        var stratagemName = stratagem.name().getString();
        var properties = stratagem.properties();

        if (!instance.isReady())
        {
            if (instance.state == StratagemState.IN_USE)
            {
                if (instance.duration != null && instance.duration > 0)
                {
                    instance.duration--;

                    if (instance.duration % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has duration: {}", stratagemName, instance.formatTickDuration(instance.duration, player));
                    }
                }
                else
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", stratagemName, instance.state, StratagemState.INBOUND);
                    instance.state = StratagemState.INBOUND;
                }
            }

            if (instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
            {
                instance.inboundDuration--;

                if (instance.inboundDuration % 20 == 0)
                {
                    LOGGER.info("{} stratagem has inboundDuration: {}", stratagemName, instance.formatTickDuration(instance.inboundDuration, player));
                }
                if (instance.inboundDuration == 0)
                {
                    if (instance.remainingUse == 0)
                    {
                        instance.state = StratagemState.DEPLETED;
                        LOGGER.info("{} stratagem is now depleted!", stratagemName);
                        return;
                    }
                    LOGGER.info("{} stratagem switch state from {} to {}", stratagemName, instance.state, StratagemState.COOLDOWN);
                    instance.state = StratagemState.COOLDOWN;
                    instance.cooldown = properties.cooldown();
                }
            }

            if (instance.state == StratagemState.COOLDOWN)
            {
                if (instance.cooldown > 0)
                {
                    instance.cooldown--;

                    if (instance.cooldown % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has cooldown: {}", stratagemName, instance.formatTickDuration(instance.cooldown, player));
                    }
                }

                if (instance.cooldown == 0)
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", stratagemName, instance.state, StratagemState.READY);
                    instance.state = StratagemState.READY;

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
        return DepletedRule::new;
    }
}