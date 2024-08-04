package com.stevekung.stratagems.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.packet.StratagemEntryData;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;

public class StratagemUtils
{
    public static boolean anyMatchHolder(Collection<StratagemInstance> list, Holder<Stratagem> stratagemHolder)
    {
        return list.stream().map(StratagemInstance::getStratagem).anyMatch(holder -> holder == stratagemHolder);
    }

    public static boolean noneMatchHolder(Collection<StratagemInstance> list, Holder<Stratagem> stratagemHolder)
    {
        return list.stream().map(StratagemInstance::getStratagem).noneMatch(holder -> holder == stratagemHolder);
    }

    public static List<StratagemInstance> mapToInstance(Collection<StratagemEntryData> entries, Function<ResourceKey<Stratagem>, Holder<Stratagem>> function)
    {
        return entries.stream().map(entry -> new StratagemInstance(function.apply(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.remainingUse(), entry.state(), entry.side())).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
    }

    public static List<StratagemEntryData> mapToEntry(Collection<StratagemInstance> list)
    {
        return list.stream().map(instance -> new StratagemEntryData(instance.getStratagem().unwrapKey().orElseThrow(), instance.inboundDuration, instance.duration, instance.cooldown, instance.remainingUse, instance.state, instance.side)).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
    }

    public static Component decorateStratagemName(Component name, Holder<Stratagem> holder)
    {
        return name.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(holder.getRegisteredName()))));
    }

    public static Component decorateStratagemList(Collection<StratagemInstance> instances)
    {
        return ComponentUtils.formatList(instances, instance -> ComponentUtils.wrapInSquareBrackets(Component.literal(instance.getResourceKey().location().toString())).withStyle(ChatFormatting.GREEN));
    }

    public static StratagemInstance createInstanceWithDefaultValue(Holder<Stratagem> stratagemHolder, StratagemInstance.Side side)
    {
        var properties = stratagemHolder.value().properties();
        return new StratagemInstance(stratagemHolder, properties.inboundDuration(), properties.duration().orElse(null), properties.cooldown(), properties.remainingUse().orElse(null), StratagemState.READY, side);
    }
}