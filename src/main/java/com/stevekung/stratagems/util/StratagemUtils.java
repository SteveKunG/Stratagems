package com.stevekung.stratagems.util;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class StratagemUtils
{
    public static List<StratagemInstance> CLIENT_STRATAGEM_LIST = Lists.newCopyOnWriteArrayList();

    public static boolean clientNoneMatch(String tempStratagemCode, Player player)
    {
        return ImmutableList.copyOf(Iterables.concat(player.getPlayerStratagems().values(), StratagemUtils.CLIENT_STRATAGEM_LIST)).stream().filter(instance -> instance.canUse(player)).noneMatch(entry -> entry.getCode().startsWith(tempStratagemCode));
    }

    public static boolean clientFoundMatch(String tempStratagemCode, Player player)
    {
        return ImmutableList.copyOf(Iterables.concat(player.getPlayerStratagems().values(), StratagemUtils.CLIENT_STRATAGEM_LIST)).stream().filter(instance -> instance.canUse(player)).anyMatch(entry -> entry.getCode().equals(tempStratagemCode));
    }

    public static Holder<Stratagem> getStratagemFromCode(String tempStratagemCode, Player player)
    {
        return ImmutableList.copyOf(Iterables.concat(player.getPlayerStratagems().values(), StratagemUtils.CLIENT_STRATAGEM_LIST)).stream().filter(entry -> entry.canUse(player) && entry.getCode().equals(tempStratagemCode)).findFirst().get().getStratagem();
    }

    public static void useStratagemImmediately(Holder<Stratagem> holder, Player player)
    {
        player.getPlayerStratagems().values().stream().filter(entry -> entry.getStratagem() == holder).findFirst().get().use(player);
    }

    public static boolean anyMatchHolder(List<StratagemInstance> list, Holder<Stratagem> stratagemHolder)
    {
        return list.stream().map(StratagemInstance::getStratagem).anyMatch(holder -> holder == stratagemHolder);
    }

    public static boolean noneMatchHolder(List<StratagemInstance> list, Holder<Stratagem> stratagemHolder)
    {
        return list.stream().map(StratagemInstance::getStratagem).noneMatch(holder -> holder == stratagemHolder);
    }

    public static CompoundTag createCompoundTagWithDefaultValue(Holder<Stratagem> stratagemHolder)
    {
        var compoundTag = new CompoundTag();
        var stratagem = stratagemHolder.value();
        var properties = stratagem.properties();
        compoundTag.putString(ModConstants.Tag.STRATAGEM, stratagemHolder.unwrapKey().orElseThrow().location().toString());
        compoundTag.putInt(ModConstants.Tag.INBOUND_DURATION, properties.inboundDuration());

        if (properties.duration().isPresent())
        {
            compoundTag.putInt(ModConstants.Tag.DURATION, properties.duration().get());
        }

        compoundTag.putInt(ModConstants.Tag.COOLDOWN, properties.cooldown());

        if (properties.remainingUse().isPresent())
        {
            compoundTag.putInt(ModConstants.Tag.REMAINING_USE, properties.remainingUse().get());
        }

        compoundTag.putString(ModConstants.Tag.STATE, StratagemState.READY.getName());
        return compoundTag;
    }

    public static StratagemInstance createInstanceWithDefaultValue(Holder<Stratagem> stratagemHolder)
    {
        var properties = stratagemHolder.value().properties();
        return new StratagemInstance(stratagemHolder, properties.inboundDuration(), properties.duration().orElse(-1), properties.cooldown(), properties.remainingUse().orElse(-1), StratagemState.READY);
    }
}