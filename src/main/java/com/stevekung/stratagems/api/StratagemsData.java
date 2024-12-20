package com.stevekung.stratagems.api;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;

public interface StratagemsData
{
    void tick();

    boolean canUse(Holder<Stratagem> holder, Player player);

    void use(Holder<Stratagem> holder, Player player);

    void add(Holder<Stratagem> holder);

    void add(Holder<Stratagem> holder, int id);

    void add(Holder<Stratagem> holder, int id, boolean shouldDisplay);

    void remove(Holder<Stratagem> holder);

    void reset(Holder<Stratagem> holder);

    void reset();

    void block(boolean unblock);

    void block(Holder<Stratagem> holder, boolean unblock);

    void modified(StratagemModifier modifier, boolean clear);

    void modified(Holder<Stratagem> holder, StratagemModifier modifier, boolean clear);

    void clear();

    Map<Holder<Stratagem>, StratagemInstance> instances();

    Collection<StratagemInstance> listInstances();

    int size();

    boolean isEmpty();

    Stream<StratagemInstance> stream();

    StratagemInstance instanceByHolder(Holder<Stratagem> holder);
}