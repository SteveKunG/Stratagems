package com.stevekung.stratagems.api.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemsData;
import com.stevekung.stratagems.api.client.ClientStratagemInstance;
import com.stevekung.stratagems.api.packet.StratagemEntryData;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
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
        return data.stream().map(StratagemInstance::getStratagem).anyMatch(holderx -> holderx == holder);
    }

    public static boolean noneMatch(StratagemsData data, Holder<Stratagem> holder)
    {
        return data.stream().map(StratagemInstance::getStratagem).noneMatch(holderx -> holderx == holder);
    }

    public static Map<Holder<Stratagem>, ClientStratagemInstance> clientMapToInstance(Collection<StratagemEntryData> entries, Function<ResourceKey<Stratagem>, Holder<Stratagem>> function)
    {
        return entries.stream().map(entry -> new ClientStratagemInstance(entry.id(), function.apply(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.lastMaxCooldown(), entry.maxUse(), entry.state(), entry.side(), entry.shouldDisplay(), entry.modifier())).collect(Collectors.toMap(StratagemInstance::getStratagem, Function.identity()));
    }

    public static List<StratagemEntryData> mapToEntry(StratagemsData stratagemsData)
    {
        return stratagemsData.stream().map(instance -> new StratagemEntryData(instance.getStratagem().unwrapKey().orElseThrow(), instance.id, instance.inboundDuration, instance.duration, instance.cooldown, instance.lastMaxCooldown, instance.maxUse, instance.state, instance.side, instance.shouldDisplay, instance.modifier)).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
    }

    public static Map<Holder<Stratagem>, ClientStratagemInstance> clientEntryToMap(Collection<StratagemEntryData> list, Function<ResourceKey<Stratagem>, Holder<Stratagem>> function)
    {
        return list.stream().collect(Collectors.toMap(entry -> function.apply(entry.stratagem()), entry -> new ClientStratagemInstance(entry.id(), function.apply(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.lastMaxCooldown(), entry.maxUse(), entry.state(), entry.side(), entry.shouldDisplay(), entry.modifier())));
    }

    public static Component decorateStratagemName(Component name, Holder<Stratagem> holder)
    {
        return name.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(holder.getRegisteredName()))));
    }

    public static Component decorateStratagemList(Collection<StratagemInstance> list)
    {
        return ComponentUtils.formatList(list, instance -> ComponentUtils.wrapInSquareBrackets(Component.literal(instance.getResourceKey().location().toString())).withStyle(ChatFormatting.GREEN));
    }

    public static String generateJammedText(String text, RandomSource randomSource, double chance, boolean onlyAlphabetChars)
    {
        var jammedText = new StringBuilder(text);

        // Iterate through the original string
        for (var i = 0; i < jammedText.length(); i++)
        {
            if (randomSource.nextDouble() < chance)
            {
                // Replace the current character with a random character
                var jammedChar = onlyAlphabetChars ? getJammedAlphabetChars() : getJammedChars();
                var randomChar = jammedChar.charAt(randomSource.nextInt(jammedChar.length()));
                jammedText.setCharAt(i, randomChar);
            }
        }
        return jammedText.toString();
    }

    public static String generateRandomizeStratagemCode(RandomSource randomSource, RegistryAccess registryAccess)
    {
        var result = new StringBuilder();

        if (randomSource.nextFloat() < 0.8f)
        {
            for (var i = 0; i < 4 + randomSource.nextInt(6); i++)
            {
                // Pick a random character from the allowed characters
                var randomChar = ModConstants.ALLOWED_CODE.charAt(randomSource.nextInt(ModConstants.ALLOWED_CODE.length()));
                result.append(randomChar);
            }
        }
        else if (randomSource.nextFloat() < 0.2f)
        {
            // Pick a random existed stratagem code in the registry
            var list = registryAccess.lookupOrThrow(ModRegistries.STRATAGEM).listElements().toList();
            return list.stream().skip(randomSource.nextInt(list.size())).findFirst().get().value().code();
        }
        else if (randomSource.nextFloat() < 0.1f)
        {
            // Pick a random character from the allowed characters
            var randomChar = ModConstants.ALLOWED_CODE.charAt(randomSource.nextInt(ModConstants.ALLOWED_CODE.length()));
            result.append(String.valueOf(randomChar).repeat(Math.max(4, randomSource.nextInt(6) + 4)));
        }
        return result.toString();
    }

    private static String getJammedChars()
    {
        return Component.translatable("stratagem.menu.jammed_uppercase").getString() + // Uppercase
                Component.translatable("stratagem.menu.jammed_lowercase").getString() + // Lowercase
                Component.translatable("stratagem.menu.jammed_numbers").getString() + // Numbers
                "!@#$%^&*()_+" + // Shifted number row
                "-=[]\\;',./" + // Unshifted
                "`~" + // Shifted backtick
                "{}|:\"<>?"; // Shifted symbols
    }

    private static String getJammedAlphabetChars()
    {
        return Component.translatable("stratagem.menu.jammed_uppercase").getString() + // Uppercase
                Component.translatable("stratagem.menu.jammed_lowercase").getString(); // Lowercase
    }
}