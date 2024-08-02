package com.stevekung.stratagems;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.command.StratagemCommands;
import com.stevekung.stratagems.entity.StratagemBall;
import com.stevekung.stratagems.packet.SpawnStratagemPacket;
import com.stevekung.stratagems.packet.UpdatePlayerStratagemsPacket;
import com.stevekung.stratagems.packet.UpdateServerStratagemsPacket;
import com.stevekung.stratagems.packet.UseReplenishStratagemPacket;
import com.stevekung.stratagems.registry.ModEntities;
import com.stevekung.stratagems.registry.ModEntityDataSerializers;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.StratagemSounds;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class StratagemsMod implements ModInitializer
{
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "stratagems";

    @Override
    public void onInitialize()
    {
        StratagemSounds.init();
        ModEntities.init();
        ModEntityDataSerializers.init();

        DynamicRegistries.registerSynced(ModRegistries.STRATAGEM, Stratagem.DIRECT_CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);
        DynamicRegistrySetupCallback.EVENT.register(registryView -> addListenerForDynamic(registryView, ModRegistries.STRATAGEM));

        PayloadTypeRegistry.playC2S().register(SpawnStratagemPacket.TYPE, SpawnStratagemPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UseReplenishStratagemPacket.TYPE, UseReplenishStratagemPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateServerStratagemsPacket.TYPE, UpdateServerStratagemsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdatePlayerStratagemsPacket.TYPE, UpdatePlayerStratagemsPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SpawnStratagemPacket.TYPE, (payload, context) ->
        {
            var player = context.player();
            var level = player.serverLevel();
            var stratagemHolder = context.server().registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(ResourceKey.create(ModRegistries.STRATAGEM, payload.stratagem()));

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            var stratagemBall = new StratagemBall(level, player);
            stratagemBall.setVariant(stratagemHolder);
            stratagemBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(stratagemBall);
        });

        ServerPlayNetworking.registerGlobalReceiver(UseReplenishStratagemPacket.TYPE, (payload, context) ->
        {
            var player = context.server().getPlayerList().getPlayer(payload.uuid());
            var holder = context.server().registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(ResourceKey.create(ModRegistries.STRATAGEM, payload.stratagem()));
            ImmutableList.copyOf(Iterables.concat(player.getPlayerStratagems().values(), context.server().overworld().getServerStratagemData().getStratagemInstances())).stream().filter(entry -> entry.getStratagem() == holder).findFirst().get().use(context.server(), player);

            for (var entry : ImmutableList.copyOf(Iterables.concat(player.getPlayerStratagems().values(), context.server().overworld().getServerStratagemData().getStratagemInstances())))
            {
                if (entry.getStratagem() == holder)
                {
                    entry.use(context.server(), player);
                    System.out.println(entry.getStratagem());
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> StratagemCommands.register(dispatcher, context));

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            var stratagemData = server.overworld().getServerStratagemData();
            stratagemData.setDirty();
            server.overworld().getDataStorage().save();
            LOGGER.info("This world has stratagem(s): {}", stratagemData.getStratagemInstances().stream().map(entry -> entry.getResourceKey().location()).toList());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            ServerPlayNetworking.send(handler.getPlayer(), UpdateServerStratagemsPacket.mapInstanceToEntry(server.overworld().getServerStratagemData().getStratagemInstances()));
            LOGGER.info("Send server stratagem packet to {}", handler.getPlayer().getName().getString());

            for (var player : server.getPlayerList().getPlayers())
            {
                ServerPlayNetworking.send(handler.getPlayer(), UpdatePlayerStratagemsPacket.mapInstanceToEntry(ImmutableList.copyOf(player.getPlayerStratagems().values()), player.getUUID()));
                LOGGER.info("Send player stratagem packet to {}", handler.getPlayer().getName().getString());
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(server ->
        {
            server.getProfiler().push("stratagemServer");

            if (server.tickRateManager().runsNormally())
            {
                server.overworld().getServerStratagemData().tick();
            }

            server.getProfiler().pop();
        });
    }

    public static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void addListenerForDynamic(DynamicRegistryView registryView, ResourceKey<? extends Registry<?>> key)
    {
        registryView.registerEntryAdded(key, (rawId, id, object) -> LOGGER.info("Loaded entry of {}: {} = {}", key, id, object));
    }
}