package com.stevekung.stratagems.api.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.packet.StratagemEntryData;
import com.stevekung.stratagems.api.packet.UpdatePlayerStratagemsPacket;
import com.stevekung.stratagems.api.packet.UpdateServerStratagemsPacket;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.StratagemRules;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

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
        var stratagems = instance.side == StratagemInstance.Side.PLAYER ? player.getStratagems().values() : server.overworld().getStratagemData().getInstances().values();
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

                if (replenishedProperties.duration() > 0)
                {
                    replenishedStratagem.duration = replenishedProperties.duration();
                }

                // replenished cooldown from replenishing properties
                replenishedStratagem.cooldown = properties.cooldown();

                if (replenishedProperties.remainingUse() > 0)
                {
                    replenishedStratagem.remainingUse = replenishedProperties.remainingUse();
                }

                LOGGER.info("Replenished {} stratagem!", replenishedStratagem.stratagem().name().getString());

                // Remove this replenished stratagem
                if (instance.side == StratagemInstance.Side.PLAYER)
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

            var replenishSoundOptional = stratagemReplenish.replenishSound();

            if (replenishSoundOptional.isPresent() && count > 0)
            {
                if (instance.side == StratagemInstance.Side.PLAYER && player instanceof ServerPlayer serverPlayer)
                {
                    serverPlayer.connection.send(new ClientboundSoundPacket(Holder.direct(replenishSoundOptional.get()), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, player.level().getRandom().nextLong()));
                }
                if (instance.side == StratagemInstance.Side.SERVER && server != null)
                {
                    server.getPlayerList().getPlayers().forEach(playerx -> playerx.connection.send(new ClientboundSoundPacket(Holder.direct(replenishSoundOptional.get()), SoundSource.PLAYERS, playerx.getX(), playerx.getY(), playerx.getZ(), 1.0f, 1.0f, playerx.level().getRandom().nextLong())));
                }
            }
        }
    }

    @Override
    public void onReset(StratagemInstanceContext context)
    {
        var instance = context.instance();

        // Remove this replenished stratagem
        if (instance.side == StratagemInstance.Side.PLAYER)
        {
            context.player().getStratagems().remove(instance.getStratagem());
            ((ServerPlayer)context.player()).connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(UpdateStratagemPacket.Action.REMOVE, StratagemEntryData.fromInstance(instance), context.player().getUUID())));
        }
        else
        {
            context.server().overworld().getStratagemData().getInstances().remove(instance.getStratagem());

            for (var serverPlayer : context.server().getPlayerList().getPlayers())
            {
                serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(UpdateStratagemPacket.Action.REMOVE, StratagemEntryData.fromInstance(instance))));
            }
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

                if (player instanceof ServerPlayer serverPlayer && instance.side == StratagemInstance.Side.PLAYER)
                {
                    var playerStratagems = serverPlayer.getStratagems();

                    if (playerStratagems.entrySet().stream().filter(entry -> toReplenish.contains(entry.getValue().getStratagem())).allMatch(entry -> entry.getValue().state == StratagemState.UNAVAILABLE))
                    {
                        this.onUse(context);
                        serverPlayer.connection.send(new ClientboundCustomPayloadPacket(UpdatePlayerStratagemsPacket.create(playerStratagems.values(), player.getUUID())));
                    }
                }
                if (server != null && instance.side == StratagemInstance.Side.SERVER)
                {
                    var serverStratagems = server.overworld().getStratagemData();

                    if (serverStratagems.getInstances().entrySet().stream().filter(entry -> toReplenish.contains(entry.getValue().getStratagem())).allMatch(entry -> entry.getValue().state == StratagemState.UNAVAILABLE))
                    {
                        this.onUse(context);

                        for (var serverPlayer : server.getPlayerList().getPlayers())
                        {
                            serverPlayer.connection.send(new ClientboundCustomPayloadPacket(UpdateServerStratagemsPacket.create(serverStratagems.getInstances().values())));
                        }
                    }
                }
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReplenishRule::new;
    }
}