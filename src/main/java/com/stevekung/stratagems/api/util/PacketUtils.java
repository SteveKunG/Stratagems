package com.stevekung.stratagems.api.util;

import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemsData;
import com.stevekung.stratagems.api.packet.SetPlayerStratagemsPacket;
import com.stevekung.stratagems.api.packet.SetServerStratagemsPacket;
import com.stevekung.stratagems.api.packet.StratagemEntryData;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketUtils
{
    /**
     * Send a stratagem update packet to the client
     */
    public static void sendClientUpdateStratagemPacket(MinecraftServer server, @Nullable ServerPlayer serverPlayer, UpdateStratagemPacket.Action action, StratagemInstance instance)
    {
        // Send a packet to a player if a stratagem is on the player
        if (serverPlayer != null)
        {
            PacketUtils.sendClientUpdatePacket2P(serverPlayer, action, instance);
        }
        else
        {
            // Send a packet to all players if a stratagem is on the server
            PacketUtils.sendClientUpdatePacketS2P(server, action, instance);
        }
    }

    /**
     * Send a stratagem update packet to a player
     */
    public static void sendClientUpdatePacket2P(ServerPlayer serverPlayer, UpdateStratagemPacket.Action action, StratagemInstance instance)
    {
        serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(action, StratagemEntryData.fromInstance(instance), serverPlayer.getUUID())));
    }

    /**
     * Send stratagem update packets from server to all players
     */
    public static void sendClientUpdatePacketS2P(MinecraftServer server, UpdateStratagemPacket.Action action, StratagemInstance instance)
    {
        for (var serverPlayer : server.getPlayerList().getPlayers())
        {
            serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(action, StratagemEntryData.fromInstance(instance))));
        }
    }

    /**
     * Send a set player stratagems packet to a player by UUID
     */
    public static void sendClientSetPlayerStratagemsPacket(ServerPlayer serverPlayer, StratagemsData stratagemsData)
    {
        serverPlayer.connection.send(new ClientboundCustomPayloadPacket(SetPlayerStratagemsPacket.create(stratagemsData, serverPlayer.getUUID())));
    }

    /**
     * Send a set server stratagems packet to all players
     */
    public static void sendClientSetServerStratagemsPacket(MinecraftServer server, StratagemsData stratagemsData)
    {
        for (var serverPlayer : server.getPlayerList().getPlayers())
        {
            serverPlayer.connection.send(new ClientboundCustomPayloadPacket(SetServerStratagemsPacket.create(stratagemsData)));
        }
    }
}