package com.stevekung.stratagems.api;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;

public interface StratagemsData
{
    void tick();

    void use(Holder<Stratagem> holder, Player player);

    void add(Holder<Stratagem> holder);

    void remove(Holder<Stratagem> holder);

    void reset(Holder<Stratagem> holder);

    void reset();

    void clear();

    Map<Holder<Stratagem>, StratagemInstance> instances();

    StratagemInstance instanceByHolder(Holder<Stratagem> holder);
}