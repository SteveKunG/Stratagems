package com.stevekung.stratagems.api.rule;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.util.PacketUtils;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.server.level.ServerPlayer;

/**
 * An advanced rule for a stratagem which can be depleted and rearm when has no remaining use left.
 */
public class DepletedAndRearmRule implements StratagemRule
{
    public static final MapCodec<DepletedAndRearmRule> CODEC = MapCodec.unit(new DepletedAndRearmRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.DEPLETED_REARM;
    }

    @Override
    public boolean canUse(StratagemInstanceContext context)
    {
        // Can use when ready and has remaining use
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

        // Check has remaining use
        if (instance.maxUse > 0)
        {
            if (replenishOptional.isPresent())
            {
                var category = replenishOptional.get().category();
                var stratagemsData = context.isServer() ? server.overworld().stratagemsData() : player.stratagemsData();

                // Set other stratagems that have the same category to IN_USE just like Eagle stratagem.
                // And check if it is not in the unavailable state.
                stratagemsData.listInstances().forEach(instancex ->
                {
                    var otherReplenishOptional = instancex.stratagem().properties().replenish();

                    if (otherReplenishOptional.isPresent() && otherReplenishOptional.get().toReplenish().isEmpty() && otherReplenishOptional.get().category().equals(category) && instancex.state != StratagemState.UNAVAILABLE)
                    {
                        instancex.state = StratagemState.IN_USE;
                    }
                });

                // Send an update packet to its side
                if (context.isServer())
                {
                    PacketUtils.sendClientSetServerStratagemsPacket(server, stratagemsData);
                }
                else
                {
                    if (player instanceof ServerPlayer serverPlayer)
                    {
                        PacketUtils.sendClientSetPlayerStratagemsPacket(serverPlayer, stratagemsData);
                    }
                }
            }

            instance.maxUse--;
            LOGGER.info("{} stratagem has maxUse: {}", stratagem.name().getString(), instance.maxUse);
        }

        // Add replenisher stratagem when max use is lower than initial value
        if (instance.maxUse < stratagem.properties().maxUse() && replenishOptional.isPresent())
        {
            var replenish = replenishOptional.get();
            var category = replenish.category();
            var replenisherOptional = replenish.replenisher();
            var stratagemsData = context.isServer() ? server.overworld().stratagemsData() : player.stratagemsData();

            // Supply same stratagem category
            Supplier<Stream<StratagemInstance>> sameReplenishStratagem = () -> stratagemsData.stream().filter(instancex -> instancex.stratagem().properties().replenish().map(stratagemReplenish -> stratagemReplenish.category().equals(category)).orElse(false));
            var sameCategoryId = sameReplenishStratagem.get().mapToInt(instancex -> instancex.id).findFirst().getAsInt();

            if (replenisherOptional.isPresent())
            {
                var replenisherKey = replenisherOptional.get();
                var registryAccess = context.isServer() ? server.registryAccess() : player.level().registryAccess();
                var replenisherStratagem = registryAccess.registryOrThrow(ModRegistries.STRATAGEM).getHolderOrThrow(replenisherKey);

                if (!StratagemUtils.anyMatch(stratagemsData, replenisherStratagem))
                {
                    // Add replenish stratagem on top of this instance
                    stratagemsData.add(replenisherStratagem, sameCategoryId - 1, instance.maxUse > 1 || sameReplenishStratagem.get().count() > 1);

                    if (context.isServer())
                    {
                        PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.ADD, stratagemsData.instanceByHolder(replenisherStratagem));
                        LOGGER.info("Add {} server replenisher stratagem", replenisherStratagem.value().name().getString());
                    }
                    else
                    {
                        if (player instanceof ServerPlayer serverPlayer)
                        {
                            PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.ADD, stratagemsData.instanceByHolder(replenisherStratagem));
                        }
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
                    instance.lastMaxCooldown = instance.cooldown;
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
        return DepletedAndRearmRule::new;
    }
}