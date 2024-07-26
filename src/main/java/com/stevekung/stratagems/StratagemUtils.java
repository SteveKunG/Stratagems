package com.stevekung.stratagems;

import java.util.List;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;

public class StratagemUtils
{
    public static List<StratagemEntry> CLIENT_STRATAGEM_LIST = Lists.newArrayList();

    public static boolean clientNoneMatch(String tempStratagemCode)
    {
        return CLIENT_STRATAGEM_LIST.stream().filter(StratagemEntry::canUse).noneMatch(entry -> entry.getCode().startsWith(tempStratagemCode));
    }

    public static boolean clientFoundMatch(String tempStratagemCode)
    {
        return CLIENT_STRATAGEM_LIST.stream().filter(StratagemEntry::canUse).anyMatch(entry -> entry.getCode().equals(tempStratagemCode));
    }

    public static ResourceKey<Stratagem> getStratagemKeyFromCode(String tempStratagemCode)
    {
        return CLIENT_STRATAGEM_LIST.stream().filter(entry -> entry.canUse() && entry.getCode().equals(tempStratagemCode)).findFirst().get().getStratagem().unwrapKey().orElseThrow();
    }

    public static boolean anyMatchHolder(List<StratagemEntry> list, Holder<Stratagem> stratagemHolder)
    {
        return list.stream().map(StratagemEntry::getStratagem).anyMatch(holder -> holder == stratagemHolder);
    }

    public static boolean noneMatchHolder(List<StratagemEntry> list, Holder<Stratagem> stratagemHolder)
    {
        return list.stream().map(StratagemEntry::getStratagem).noneMatch(holder -> holder == stratagemHolder);
    }

    public static CompoundTag createCompoundTagWithDefaultValue(Holder<Stratagem> stratagemHolder)
    {
        var compoundTag = new CompoundTag();
        var stratagem = stratagemHolder.value();
        var properties = stratagem.properties();
        compoundTag.putString(ModConstants.Tag.STRATAGEM, stratagemHolder.unwrapKey().orElseThrow().location().toString());
        compoundTag.putInt(ModConstants.Tag.INCOMING_DURATION, properties.incomingDuration());

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
}