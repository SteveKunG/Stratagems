package com.stevekung.stratagems;

import java.util.Optional;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public record StratagemInstanceContext(StratagemInstance instance, Optional<MinecraftServer> minecraftServer, Optional<Player> player)
{
    public static StratagemInstanceContext create(StratagemInstance instance, MinecraftServer minecraftServer, Player player)
    {
        return new StratagemInstanceContext(instance, Optional.ofNullable(minecraftServer), Optional.ofNullable(player));
    }
}