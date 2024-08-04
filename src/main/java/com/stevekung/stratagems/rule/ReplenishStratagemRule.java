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

public class ReplenishStratagemRule implements StratagemRule
{
    public static final MapCodec<ReplenishStratagemRule> CODEC = MapCodec.unit(new ReplenishStratagemRule());
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
        var player = context.player().orElse(null);
        var stratagemData = instance.side == Side.PLAYER ? player.getPlayerStratagems().values() : context.minecraftServer().get().overworld().getServerStratagemData().getStratagemInstances();
        var rearmProperties = instance.stratagem().properties();
        var count = 0;

        if (rearmProperties.replenish().isPresent() && rearmProperties.replenish().get().toReplenish().isPresent())
        {
            for (var toReplenishinstance : stratagemData.stream().filter(toReplenishinstance -> rearmProperties.replenish().get().toReplenish().get().contains(toReplenishinstance.getStratagem())).toList())
            {
                toReplenishinstance.state = StratagemState.COOLDOWN;

                var replenishedProperties = toReplenishinstance.stratagem().properties();
                toReplenishinstance.inboundDuration = replenishedProperties.inboundDuration();

                if (replenishedProperties.duration().isPresent())
                {
                    toReplenishinstance.duration = replenishedProperties.duration().get();
                }

                // replenished cooldown from rearm
                toReplenishinstance.cooldown = rearmProperties.cooldown();

                if (replenishedProperties.remainingUse().isPresent())
                {
                    toReplenishinstance.remainingUse = replenishedProperties.remainingUse().get();
                }

                LOGGER.info("Replenished {} stratagem!", toReplenishinstance.stratagem().name().getString());

                // Remove this rearm stratagem
                if (instance.side == Side.PLAYER)
                {
                    player.getPlayerStratagems().remove(instance.getStratagem());
                }
                else
                {
                    context.minecraftServer().get().overworld().getServerStratagemData().remove(instance.getStratagem());
                }

                LOGGER.info("Remove {} replenisher stratagem!", instance.stratagem().name().getString());
                count++;
            }

            if (player != null && rearmProperties.replenish().get().replenishSound().isPresent() && count > 0)
            {
                player.playSound(rearmProperties.replenish().get().replenishSound().get(), 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void onReset(StratagemInstanceContext context)
    {
        context.player().get().getPlayerStratagems().remove(context.instance().getStratagem());
        LOGGER.info("Remove {} replenisher stratagem on reset!", context.instance().stratagem().name().getString());
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        if (context.minecraftServer().isPresent() && context.instance().stratagem().properties().replenish().isPresent())
        {
            if (context.instance().stratagem().properties().replenish().get().toReplenish().isPresent())
            {
                var instance = context.instance();
                var player = context.player().orElse(null);

                if (instance.side == Side.PLAYER)
                {
                    var depletedStratagem = player.getPlayerStratagems().entrySet().stream().filter(entry ->
                    {
                        return entry.getValue().state == StratagemState.DEPLETED && context.instance().stratagem().properties().replenish().get().toReplenish().get().contains(entry.getKey());
                    });

                    if (depletedStratagem.findAny().isPresent())
                    {
                        context.instance().use(context.minecraftServer().get(), player);
                        ServerPlayNetworking.send((ServerPlayer)player, UpdatePlayerStratagemsPacket.create(player.getPlayerStratagems().values(), player.getUUID()));
                    }
                }
                else
                {
                    var depletedStratagem = context.minecraftServer().get().overworld().getServerStratagemData().getStratagemInstances().stream().filter(instancex ->
                    {
                        return instancex.state == StratagemState.DEPLETED && context.instance().stratagem().properties().replenish().get().toReplenish().get().contains(instancex.getStratagem());
                    });

                    if (depletedStratagem.findAny().isPresent())
                    {
                        context.instance().use(context.minecraftServer().get(), player);
                        
                        for (var serverPlayer : PlayerLookup.all(context.minecraftServer().get()))
                        {
                            ServerPlayNetworking.send(serverPlayer, UpdateServerStratagemsPacket.create(context.minecraftServer().get().overworld().getServerStratagemData().getStratagemInstances()));
                        }
                    }
                }
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReplenishStratagemRule::new;
    }
}