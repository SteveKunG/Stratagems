package com.stevekung.stratagems.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public record StratagemInstanceContext(StratagemInstance instance, @Nullable MinecraftServer server, @Nullable Player player, boolean isServer)
{
    public static StratagemInstanceContext create(StratagemInstance instance, MinecraftServer minecraftServer, Player player, boolean isServer)
    {
        return new StratagemInstanceContext(instance, minecraftServer, player, isServer);
    }
}