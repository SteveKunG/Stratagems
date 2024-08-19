package com.stevekung.stratagems;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
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
            var player = server.getPlayerList().getPlayer(payload.uuid());
            var serverStratagems = server.overworld().stratagemsData();
            var playerStratagems = player.stratagemsData();
            var holder = server.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(payload.stratagem());

            if (payload.side() == StratagemInstance.Side.PLAYER)
            {
                playerStratagems.use(holder, player);
                ServerPlayNetworking.send(player, UpdatePlayerStratagemsPacket.create(playerStratagems.instances().values(), player.getUUID()));
            }
            else
            {
                serverStratagems.use(holder, player);

                for (var serverPlayer : PlayerLookup.all(server))
                {
                    ServerPlayNetworking.send(serverPlayer, UpdateServerStratagemsPacket.create(serverStratagems.instances().values()));
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> StratagemCommands.register(dispatcher, context));

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            var serverStratagems = server.overworld().stratagemsData();
            serverStratagems.setDirty();
            server.overworld().getDataStorage().save();
            ModConstants.LOGGER.info("This world has {} stratagem(s): {}", serverStratagems.instances().size(), serverStratagems.instances().values().stream().map(instance -> instance.getResourceKey().location()).toList());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            var player = handler.getPlayer();
            var playerStratagems = player.stratagemsData().instances().values();
            var serverStratagems = server.overworld().stratagemsData().instances().values();

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