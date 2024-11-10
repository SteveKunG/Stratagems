package com.stevekung.stratagems.api.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.util.PacketUtils;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.server.level.ServerPlayer;

/**
 * An advanced rule for a stratagem which can be removed when has no remaining use left.
 */
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
        // Can use when ready and has remaining use
        return context.instance().isReady() && context.instance().maxUse > 0;
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var stratagem = instance.stratagem();

        // Check has remaining use
        if (instance.maxUse > 0)
        {
            context.instance().state = StratagemState.IN_USE;
            instance.maxUse--;
            LOGGER.info("{} stratagem has maxUse: {}", stratagem.name().getString(), instance.maxUse);
        }
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var server = context.server();
        var stratagem = instance.stratagem();
        var stratagemName = stratagem.name().getString();
        var properties = stratagem.properties();
        var level = context.isServer() ? server.overworld() : player.level();
        var stratagemsData = context.isServer() ? server.overworld().stratagemsData() : player.stratagemsData();

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
                        // Send an update packet to its side
                        if (context.isServer())
                        {
                            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.REMOVE, instance);
                        }
                        else
                        {
                            if (player instanceof ServerPlayer serverPlayer)
                            {
                                PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.REMOVE, instance);
                            }
                        }
                        stratagemsData.remove(instance.getStratagem());
                        LOGGER.info("{} stratagem has been removed", stratagem.name().getString());
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
        return DepletedRule::new;
    }
}