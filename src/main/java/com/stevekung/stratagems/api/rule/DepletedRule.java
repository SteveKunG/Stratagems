package com.stevekung.stratagems.api.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
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
                var stratagemsData = context.isServer() ? server.overworld().stratagemsData().listInstances() : player.stratagemsData().listInstances();

                stratagemsData.forEach(instancex ->
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
            var replenish = replenishOptional.get();
            var category = replenish.category();
            var replenisherOptional = replenish.replenisher();
            var stratagemsData = context.isServer() ? server.overworld().stratagemsData() : player.stratagemsData();

            if (replenisherOptional.isPresent())
            {
                var replenisherKey = replenisherOptional.get();
                var registryAccess = context.isServer() ? server.registryAccess() : player.level().registryAccess();
                var replenisherStratagem = registryAccess.registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(replenisherKey);
                var sameCategoryId = stratagemsData.stream().filter(instancex -> instancex.stratagem().properties().replenish().map(stratagemReplenish -> stratagemReplenish.category().equals(category)).orElse(false)).mapToInt(instancex -> instancex.id).findFirst().getAsInt();

                if (!StratagemUtils.anyMatch(stratagemsData, replenisherStratagem))
                {
                    // Add replenish stratagem on top of this instance
                    stratagemsData.add(replenisherStratagem, sameCategoryId - 1);

                    if (context.isServer())
                    {
                        LOGGER.info("Add {} server replenisher stratagem", replenisherStratagem.value().name().getString());
                    }
                    else
                    {
                        LOGGER.info("Add {} replenisher stratagem to {}", replenisherStratagem.value().name().getString(), player.getName().getString());
                    }
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
        var level = context.isServer() ? context.server().overworld() : player.level();

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