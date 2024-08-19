package com.stevekung.stratagems.api.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.util.StratagemUtils;

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
        return context.instance().isReady() && context.instance().maxUse > 0;
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var server = context.server();
        var stratagem = instance.stratagem();
        var replenishOptional = stratagem.properties().replenish();

        if (instance.maxUse > 0)
        {
            // Set state from READY to IN_USE
            if (replenishOptional.isPresent())
            {
                var category = replenishOptional.get().category();
                var stratagems = instance.side == StratagemInstance.Side.PLAYER ? player.stratagemsData().instances().values() : server.overworld().stratagemsData().instances().values();

                stratagems.forEach(instancex ->
                {
                    var otherReplenishOptional = instancex.stratagem().properties().replenish();
                    
                    if (otherReplenishOptional.isPresent() && otherReplenishOptional.get().toReplenish().isEmpty() && otherReplenishOptional.get().category().equals(category) && instancex.state != StratagemState.UNAVAILABLE)
                    {
                        instancex.state = StratagemState.IN_USE;
                    }
                });
            }

            instance.maxUse--;
            LOGGER.info("{} stratagem has maxUse: {}", stratagem.name().getString(), instance.maxUse);
        }

        // Add replenisher stratagem when max use is lower than original
        if (instance.maxUse < stratagem.properties().maxUse() && replenishOptional.isPresent())
        {
            var replenisherOptional = replenishOptional.get().replenisher();

            if (replenisherOptional.isPresent())
            {
                var replenisherKey = replenisherOptional.get();

                if (instance.side == StratagemInstance.Side.PLAYER && player != null)
                {
                    var playerStratagems = player.stratagemsData();
                    var replenisherStratagem = player.level().registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(replenisherKey);

                    if (StratagemUtils.anyMatch(playerStratagems, replenisherStratagem))
                    {
                        LOGGER.info("{} player replenisher stratagem already exist", replenisherStratagem.value().name().getString());
                        return;
                    }

                    playerStratagems.add(replenisherStratagem);
                    LOGGER.info("Add {} replenisher stratagem to {}", replenisherStratagem.value().name().getString(), player.getName().getString());
                }
                if (instance.side == StratagemInstance.Side.SERVER && server != null)
                {
                    var serverStratagems = server.overworld().stratagemsData();
                    var replenisherStratagem = server.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(replenisherKey);

                    if (StratagemUtils.anyMatch(serverStratagems, replenisherStratagem))
                    {
                        LOGGER.info("{} server replenisher stratagem already exist", replenisherStratagem.value().name().getString());
                        return;
                    }

                    serverStratagems.add(replenisherStratagem);
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
        var level = player != null ? player.level() : context.server().overworld();

        if (!instance.isReady())
        {
            if (instance.state == StratagemState.IN_USE)
            {
                if (instance.duration > 0)
                {
                    instance.duration--;

                    if (instance.duration % 20 == 0)
                    {
                        LOGGER.info("{} stratagem has duration: {}", stratagemName, StratagemUtils.formatTickDuration(instance.duration, level));
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
                    LOGGER.info("{} stratagem has inboundDuration: {}", stratagemName, StratagemUtils.formatTickDuration(instance.inboundDuration, level));
                }
                if (instance.inboundDuration == 0)
                {
                    if (instance.maxUse == 0)
                    {
                        instance.state = StratagemState.UNAVAILABLE;
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
                        LOGGER.info("{} stratagem has cooldown: {}", stratagemName, StratagemUtils.formatTickDuration(instance.cooldown, level));
                    }
                }

                if (instance.cooldown == 0)
                {
                    LOGGER.info("{} stratagem switch state from {} to {}", stratagemName, instance.state, StratagemState.READY);
                    instance.state = StratagemState.READY;

                    instance.inboundDuration = properties.inboundDuration();

                    if (properties.duration() > 0)
                    {
                        instance.duration = properties.duration();
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