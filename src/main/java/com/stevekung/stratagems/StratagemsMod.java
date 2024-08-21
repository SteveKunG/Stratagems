package com.stevekung.stratagems;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.ServerStratagemsData;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.packet.*;
import com.stevekung.stratagems.api.references.ModEntityDataSerializers;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.references.StratagemSounds;
import com.stevekung.stratagems.command.StratagemCommands;
import com.stevekung.stratagems.entity.StratagemBall;
import com.stevekung.stratagems.registry.ModEntities;
import com.stevekung.stratagems.registry.StratagemActionTypes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class StratagemsMod implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        StratagemSounds.init();
        ModEntities.init();
        ModEntityDataSerializers.init();
        StratagemActionTypes.init();
        StratagemRules.init();

        DynamicRegistries.registerSynced(ModRegistries.STRATAGEM, Stratagem.DIRECT_CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);
        DynamicRegistrySetupCallback.EVENT.register(StratagemsMod::addListenerForDynamic);

        PayloadTypeRegistry.playC2S().register(SpawnStratagemPacket.TYPE, SpawnStratagemPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UseReplenishStratagemPacket.TYPE, UseReplenishStratagemPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdatePlayerStratagemsPacket.TYPE, UpdatePlayerStratagemsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateServerStratagemsPacket.TYPE, UpdateServerStratagemsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateStratagemPacket.TYPE, UpdateStratagemPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearStratagemsPacket.TYPE, ClearStratagemsPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SpawnStratagemPacket.TYPE, (payload, context) ->
        {
            var player = context.player();
            var level = player.serverLevel();
            var holder = context.server().registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(payload.stratagem());

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            var stratagemBall = new StratagemBall(level, player);
            stratagemBall.setVariant(holder);
            stratagemBall.setSide(payload.side());
            stratagemBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(stratagemBall);
        });

        ServerPlayNetworking.registerGlobalReceiver(UseReplenishStratagemPacket.TYPE, (payload, context) ->
        {
            var server = context.server();
            var level = context.server().getLevel(payload.dimension());
            var player = server.getPlayerList().getPlayer(payload.uuid());
            var stratagemsData = payload.side() == StratagemInstance.Side.PLAYER ? player.stratagemsData() : server.overworld().stratagemsData();
            var holder = server.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(payload.stratagem());

            if (stratagemsData.canUse(holder, player))
            {
                var stratagemContext = new StratagemActionContext(player, level, payload.blockPos(), level.random);
                holder.value().action().action(stratagemContext);
                stratagemsData.use(holder, player);

                if (payload.side() == StratagemInstance.Side.PLAYER)
                {
                    ServerPlayNetworking.send(player, UpdatePlayerStratagemsPacket.create(stratagemsData, player.getUUID()));
                }
                else
                {
                    for (var serverPlayer : PlayerLookup.all(server))
                    {
                        ServerPlayNetworking.send(serverPlayer, UpdateServerStratagemsPacket.create(stratagemsData));
                    }
                }
            }
            else
            {
                var instance = stratagemsData.instanceByHolder(holder);
                ModConstants.LOGGER.info("{}", Component.translatable("commands.stratagem.use.failed", instance.stratagem().name(), instance.state).getString());
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> StratagemCommands.register(dispatcher, context));

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            var serverStratagems = server.overworld().stratagemsData();
            ((ServerStratagemsData)serverStratagems).setDirty();
            server.overworld().getDataStorage().save();
            ModConstants.LOGGER.info("This world has {} stratagem(s): {}", serverStratagems.size(), serverStratagems.stream().map(instance -> instance.getResourceKey().location()).toList());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            var player = handler.getPlayer();
            var playerStratagems = player.stratagemsData();
            var serverStratagems = server.overworld().stratagemsData();

            ServerPlayNetworking.send(player, UpdatePlayerStratagemsPacket.create(playerStratagems, player.getUUID()));
            ServerPlayNetworking.send(player, UpdateServerStratagemsPacket.create(serverStratagems));
            ModConstants.LOGGER.info("Send server stratagem packet to {} in total {}", player.getName().getString(), serverStratagems.size());
            ModConstants.LOGGER.info("Send player stratagem packet to {} in total {}", player.getName().getString(), playerStratagems.size());
        });

        ServerTickEvents.START_SERVER_TICK.register(server ->
        {
            server.getProfiler().push("stratagemServer");

            if (server.tickRateManager().runsNormally())
            {
                server.overworld().stratagemsData().tick();
            }

            server.getProfiler().pop();
        });
    }

    private static void addListenerForDynamic(DynamicRegistryView registryView)
    {
        registryView.registerEntryAdded(ModRegistries.STRATAGEM, (rawId, id, object) -> ModConstants.LOGGER.info("Loaded entry of {}: {} = {}", ModRegistries.STRATAGEM, id, object));
    }
}