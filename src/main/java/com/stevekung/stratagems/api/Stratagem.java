package com.stevekung.stratagems.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.references.StratagemActions;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.rule.DefaultRule;
import com.stevekung.stratagems.api.rule.StratagemRule;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record Stratagem(String code, Component name, StratagemDisplay display, StratagemAction action, StratagemRule rule, StratagemProperties properties)
{
    public static final Codec<Stratagem> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_EMPTY_STRING.validate(Stratagem::validateStratagemCode).fieldOf("code").forGetter(Stratagem::code),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Stratagem::name),
            StratagemDisplay.CODEC.fieldOf("display").forGetter(Stratagem::display),
            StratagemActions.DIRECT_CODEC.fieldOf("action").forGetter(Stratagem::action),
            StratagemRules.CODEC.optionalFieldOf("rule", DefaultRule.defaultRule().build()).forGetter(Stratagem::rule),
            StratagemProperties.CODEC.fieldOf("properties").forGetter(Stratagem::properties)
            ).apply(instance, Stratagem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Stratagem>> STREAM_CODEC = ByteBufCodecs.holderRegistry(ModRegistries.STRATAGEM);

    private static DataResult<String> validateStratagemCode(String value)
    {
        if (value.matches("[wasd]+"))
        {
            return DataResult.success(value);
        }
        else
        {
            return DataResult.error(() -> "Stratagem code contains invalid character: %s".formatted(value));
        }
    }
}