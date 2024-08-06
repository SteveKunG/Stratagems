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
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;

public class StratagemUtils
{
    public static String formatTickDuration(int duration, Level level)
    {
        return Component.translatable("stratagem.menu.tminus").getString() + StringUtil.formatTickDuration(duration, level.tickRateManager().tickrate());
    }

    public static boolean anyMatchHolder(Collection<StratagemInstance> list, Holder<Stratagem> holder)
    {
        return list.stream().map(StratagemInstance::getStratagem).anyMatch(holderx -> holder == holderx);
    }

    public static boolean noneMatchHolder(Collection<StratagemInstance> list, Holder<Stratagem> holder)
    {
        return list.stream().map(StratagemInstance::getStratagem).noneMatch(holderx -> holder == holderx);
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

    public static Component decorateStratagemList(Collection<StratagemInstance> list)
    {
        return ComponentUtils.formatList(list, instance -> ComponentUtils.wrapInSquareBrackets(Component.literal(instance.getResourceKey().location().toString())).withStyle(ChatFormatting.GREEN));
    }

    public static StratagemInstance createInstanceWithDefaultValue(Holder<Stratagem> holder, StratagemInstance.Side side)
    {
        var properties = holder.value().properties();
        return new StratagemInstance(holder, properties.inboundDuration(), properties.duration().orElse(null), properties.cooldown(), properties.remainingUse().orElse(null), StratagemState.READY, side);
    }
}