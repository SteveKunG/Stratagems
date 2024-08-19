package com.stevekung.stratagems.api.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.StratagemsData;
import com.stevekung.stratagems.api.packet.StratagemEntryData;
import com.stevekung.stratagems.api.references.ModRegistries;

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

    public static boolean anyMatch(StratagemsData data, Holder<Stratagem> holder)
    {
        return data.instances().values().stream().map(StratagemInstance::getStratagem).anyMatch(holderx -> holderx == holder);
    }

    public static boolean noneMatch(StratagemsData data, Holder<Stratagem> holder)
    {
        return data.instances().values().stream().map(StratagemInstance::getStratagem).noneMatch(holderx -> holderx == holder);
    }

    public static Map<Holder<Stratagem>, StratagemInstance> mapToInstance(Collection<StratagemEntryData> entries, Function<ResourceKey<Stratagem>, Holder<Stratagem>> function)
    {
        return entries.stream().map(entry -> new StratagemInstance(entry.id(), function.apply(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.maxUse(), entry.state(), entry.side())).collect(Collectors.toMap(StratagemInstance::getStratagem, Function.identity()));
    }

    public static List<StratagemEntryData> mapToEntry(Collection<StratagemInstance> list)
    {
        return list.stream().map(instance -> new StratagemEntryData(instance.getStratagem().unwrapKey().orElseThrow(), instance.id, instance.inboundDuration, instance.duration, instance.cooldown, instance.maxUse, instance.state, instance.side)).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
    }

    public static Map<Holder<Stratagem>, StratagemInstance> entryToMap(Collection<StratagemEntryData> list, Level level)
    {
        Function<ResourceKey<Stratagem>, Holder<Stratagem>> function = resourceKey -> level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey);
        return list.stream().collect(Collectors.toMap(entry -> function.apply(entry.stratagem()), entry -> new StratagemInstance(entry.id(), function.apply(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.maxUse(), entry.state(), entry.side())));
    }

    public static Component decorateStratagemName(Component name, Holder<Stratagem> holder)
    {
        return name.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(holder.getRegisteredName()))));
    }

    public static Component decorateStratagemList(Collection<StratagemInstance> list)
    {
        return ComponentUtils.formatList(list, instance -> ComponentUtils.wrapInSquareBrackets(Component.literal(instance.getResourceKey().location().toString())).withStyle(ChatFormatting.GREEN));
    }

    public static StratagemInstance createInstanceForPlayer(Holder<Stratagem> holder, int id)
    {
        var properties = holder.value().properties();
        return new StratagemInstance(id, holder, properties.inboundDuration(), properties.duration(), properties.cooldown(), properties.maxUse(), StratagemState.READY, StratagemInstance.Side.PLAYER);
    }
}