package com.stevekung.stratagems;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.action.StratagemAction;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.StratagemActions;
import com.stevekung.stratagems.registry.StratagemRules;
import com.stevekung.stratagems.rule.DefaultRule;
import com.stevekung.stratagems.rule.StratagemRule;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record Stratagem(String code, Component name, Either<ItemStack, ResourceLocation> icon, StratagemAction action, StratagemRule rule, StratagemProperties properties)
{
    public static final Codec<Stratagem> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_EMPTY_STRING.validate(Stratagem::validateStratagemCode).fieldOf("code").forGetter(Stratagem::code),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Stratagem::name),
            Codec.either(ItemStack.CODEC, ResourceLocation.CODEC).fieldOf("icon").forGetter(Stratagem::icon),
            StratagemActions.DIRECT_CODEC.fieldOf("action").forGetter(Stratagem::action),
            StratagemRules.CODEC.optionalFieldOf("rule", DefaultRule.defaultRule().build()).forGetter(Stratagem::rule),
            StratagemProperties.CODEC.fieldOf("properties").forGetter(Stratagem::properties)
            ).apply(instance, Stratagem::new));
    public static final Codec<Holder<Stratagem>> CODEC = RegistryFileCodec.create(ModRegistries.STRATAGEM, DIRECT_CODEC);
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