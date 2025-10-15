package com.stevekung.stratagems.api;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public interface StratagemsData
{
    void tick(@Nullable MinecraftServer server);

    boolean canUse(@Nullable MinecraftServer server, Holder<Stratagem> holder, Player player);

    void use(@Nullable MinecraftServer server, Holder<Stratagem> holder, Player player);

    void add(Holder<Stratagem> holder);

    void add(Holder<Stratagem> holder, int id);

    void add(Holder<Stratagem> holder, int id, boolean shouldDisplay);

    void remove(Holder<Stratagem> holder);

    void reset(@Nullable MinecraftServer server, Holder<Stratagem> holder);

    void reset(@Nullable MinecraftServer server);

    void block(@Nullable MinecraftServer server, boolean unblock);

    void block(@Nullable MinecraftServer server, Holder<Stratagem> holder, boolean unblock);

    void modified(@Nullable MinecraftServer server, StratagemModifier modifier, boolean clear);

    void modified(@Nullable MinecraftServer server, Holder<Stratagem> holder, StratagemModifier modifier, boolean clear);

    void clear();

    Map<Holder<Stratagem>, StratagemInstance> instances();

    Collection<StratagemInstance> listInstances();

    int size();

    boolean isEmpty();

    Stream<StratagemInstance> stream();

    StratagemInstance instanceByHolder(Holder<Stratagem> holder);
}