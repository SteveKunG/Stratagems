package com.stevekung.stratagems.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemInstance.Side;
import com.stevekung.stratagems.StratagemInstanceContext;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.packet.UpdatePlayerStratagemsPacket;
import com.stevekung.stratagems.packet.UpdateServerStratagemsPacket;
import com.stevekung.stratagems.registry.StratagemRules;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ReplenishRule implements StratagemRule
{
    public static final MapCodec<ReplenishRule> CODEC = MapCodec.unit(new ReplenishRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.REPLENISH;
    }

    @Override
    public boolean canUse(StratagemInstanceContext context)
    {
        return context.instance().isReady();
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var server = context.server();
        var stratagems = instance.side == Side.PLAYER ? player.getStratagems().values() : server.overworld().getStratagemData().getInstances();
        var properties = instance.stratagem().properties();
        var replenishOptional = properties.replenish();
        var count = 0;

        if (replenishOptional.isPresent() && replenishOptional.get().toReplenish().isPresent())
        {
            var stratagemReplenish = replenishOptional.get();
            var toReplenishSet = stratagemReplenish.toReplenish().get();

            for (var replenishedStratagem : stratagems.stream().filter(stratagem -> toReplenishSet.contains(stratagem.getStratagem())).toList())
            {
                replenishedStratagem.state = StratagemState.COOLDOWN;

                var replenishedProperties = replenishedStratagem.stratagem().properties();
                replenishedStratagem.inboundDuration = replenishedProperties.inboundDuration();

                if (replenishedProperties.duration().isPresent())
                {
                    replenishedStratagem.duration = replenishedProperties.duration().get();
                }

                // replenished cooldown from rearm
                replenishedStratagem.cooldown = properties.cooldown();

                if (replenishedProperties.remainingUse().isPresent())
                {
                    replenishedStratagem.remainingUse = replenishedProperties.remainingUse().get();
                }

                LOGGER.info("Replenished {} stratagem!", replenishedStratagem.stratagem().name().getString());

                // Remove this rearm stratagem
                if (instance.side == Side.PLAYER)
                {
                    player.getStratagems().remove(instance.getStratagem());
                }
                else
                {
                    server.overworld().getStratagemData().remove(instance.getStratagem());
                }

                LOGGER.info("Remove {} replenisher stratagem!", instance.stratagem().name().getString());
                count++;
            }

            if (player != null && stratagemReplenish.replenishSound().isPresent() && count > 0)
            {
                player.playSound(stratagemReplenish.replenishSound().get(), 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void onReset(StratagemInstanceContext context)
    {
        var instance = context.instance();

        // Remove this rearm stratagem
        if (instance.side == Side.PLAYER)
        {
            context.player().getStratagems().remove(instance.getStratagem());
        }
        else
        {
            context.server().overworld().getStratagemData().remove(instance.getStratagem());
        }

        LOGGER.info("Remove {} replenisher stratagem on reset!", instance.stratagem().name().getString());
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var properties = instance.stratagem().properties();
        var replenishOptional = properties.replenish();

        if (replenishOptional.isPresent())
        {
            var replenish = replenishOptional.get();
            var toReplenishOptional = replenish.toReplenish();

            if (toReplenishOptional.isPresent())
            {
                var toReplenish = toReplenishOptional.get();
                var player = context.player();
                var server = context.server();
                var serverStratagems = server.overworld().getStratagemData();
                var playerStratagems = player.getStratagems();

                if (player != null && instance.side == Side.PLAYER)
                {
                    playerStratagems.entrySet().stream().filter(entry -> entry.getValue().state == StratagemState.DEPLETED && toReplenish.contains(entry.getKey())).findAny().ifPresent(ignore ->
                    {
                        instance.use(null, player);
                        ServerPlayNetworking.send((ServerPlayer)player, UpdatePlayerStratagemsPacket.create(playerStratagems.values(), player.getUUID()));
                    });
                }
                if (server != null && instance.side == Side.SERVER)
                {
                    serverStratagems.getInstances().stream().filter(instancex -> instancex.state == StratagemState.DEPLETED && toReplenish.contains(instancex.getStratagem())).findAny().ifPresent(ignore ->
                    {
                        instance.use(server, player);

                        for (var serverPlayer : PlayerLookup.all(server))
                        {
                            ServerPlayNetworking.send(serverPlayer, UpdateServerStratagemsPacket.create(serverStratagems.getInstances()));
                        }
                    });
                }
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReplenishRule::new;
    }
}