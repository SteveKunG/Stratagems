package com.stevekung.stratagems.api.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.util.PacketUtils;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

/**
 * An advanced stratagem rule that replenishes stratagems which depleted state with the same category
 */
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
        var stratagemsData = context.isServer() ? server.overworld().stratagemsData() : player.stratagemsData();
        var properties = instance.stratagem().properties();
        var replenishOptional = properties.replenish();
        var count = 0;

        if (replenishOptional.isPresent() && replenishOptional.get().toReplenish().isPresent())
        {
            var stratagemReplenish = replenishOptional.get();
            var toReplenishSet = stratagemReplenish.toReplenish().get();

            // Check other stratagems are contains in toReplenish() tag.
            for (var replenishedStratagem : stratagemsData.stream().filter(stratagem -> toReplenishSet.contains(stratagem.getStratagem())).toList())
            {
                replenishedStratagem.state = StratagemState.COOLDOWN;

                var replenishedProperties = replenishedStratagem.stratagem().properties();
                replenishedStratagem.inboundDuration = replenishedProperties.inboundDuration();

                if (replenishedProperties.duration() > 0)
                {
                    replenishedStratagem.duration = replenishedProperties.duration();
                }

                // Replenished cooldown from this stratagem properties
                replenishedStratagem.cooldown = properties.cooldown();
                replenishedStratagem.lastMaxCooldown = properties.cooldown();

                if (replenishedProperties.maxUse() > 0)
                {
                    replenishedStratagem.maxUse = replenishedProperties.maxUse();
                }

                LOGGER.info("Replenished {} stratagem!", replenishedStratagem.stratagem().name().getString());

                // Remove this replenished stratagem
                stratagemsData.remove(instance.getStratagem());

                LOGGER.info("Remove {} replenisher stratagem!", instance.stratagem().name().getString());
                count++;
            }

            var replenishSoundOptional = stratagemReplenish.replenishSound();

            // If the replenished stratagem count is more than 0, send only one play sound packet.
            if (replenishSoundOptional.isPresent() && count > 0)
            {
                if (context.isServer())
                {
                    server.getPlayerList().getPlayers().forEach(playerx -> playerx.connection.send(new ClientboundSoundPacket(Holder.direct(replenishSoundOptional.get()), SoundSource.PLAYERS, playerx.getX(), playerx.getY(), playerx.getZ(), 1.0f, 1.0f, playerx.level().getRandom().nextLong())));
                }
                else
                {
                    if (player instanceof ServerPlayer serverPlayer)
                    {
                        serverPlayer.connection.send(new ClientboundSoundPacket(Holder.direct(replenishSoundOptional.get()), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, player.level().getRandom().nextLong()));
                    }
                }
            }
        }
    }

    @Override
    public void onReset(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var stratagemsData = context.isServer() ? context.server().overworld().stratagemsData() : player.stratagemsData();

        // Send an update packet to its side
        if (context.isServer())
        {
            PacketUtils.sendClientUpdatePacketS2P(context.server(), UpdateStratagemPacket.Action.REMOVE, stratagemsData.instanceByHolder(instance.getStratagem()));
        }
        else
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.REMOVE, stratagemsData.instanceByHolder(instance.getStratagem()));
            }
        }

        // Remove this replenished stratagem
        stratagemsData.remove(instance.getStratagem());

        LOGGER.info("Remove {} replenisher stratagem on reset!", instance.stratagem().name().getString());
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();
        var server = context.server();
        var properties = instance.stratagem().properties();
        var replenishOptional = properties.replenish();

        if (replenishOptional.isPresent())
        {
            var category = replenishOptional.get().category();
            var stratagemsData = context.isServer() ? server.overworld().stratagemsData() : player.stratagemsData();

            // Check if all the stratagems are depleted then use this replenished stratagem
            if (stratagemsData.stream().filter(instancex ->
            {
                var otherReplenishOptional = instancex.stratagem().properties().replenish();
                return otherReplenishOptional.isPresent() && otherReplenishOptional.get().toReplenish().isEmpty() && otherReplenishOptional.get().category().equals(category);
            }).allMatch(instancex -> instancex.state == StratagemState.UNAVAILABLE))
            {
                this.onUse(context);

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
        }
    }

    public static Builder defaultRule()
    {
        return ReplenishRule::new;
    }
}