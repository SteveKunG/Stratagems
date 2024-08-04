package com.stevekung.stratagems;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public record StratagemInstanceContext(StratagemInstance instance, @Nullable MinecraftServer server, @Nullable Player player)
{
    public static StratagemInstanceContext create(StratagemInstance instance, MinecraftServer minecraftServer, Player player)
    {
        return new StratagemInstanceContext(instance, minecraftServer, player);
    }
}