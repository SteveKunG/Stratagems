package com.stevekung.stratagems;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record Stratagem(String code, Component name, Either<ItemStack, ResourceLocation> icon, int incomingDuration, Optional<Integer> duration, int nextUseCooldown)
{
    public static final Codec<Stratagem> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_EMPTY_STRING.flatXmap(Stratagem::validateStratagemCode, Stratagem::validateStratagemCode).fieldOf("code").forGetter(Stratagem::code),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Stratagem::name),
            Codec.either(ItemStack.CODEC, ResourceLocation.CODEC).fieldOf("icon").forGetter(Stratagem::icon),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("incoming_duration").forGetter(Stratagem::incomingDuration),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("duration").forGetter(Stratagem::duration),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("next_use_cooldown").forGetter(Stratagem::nextUseCooldown)
    ).apply(instance, Stratagem::new));
    public static final Codec<Holder<Stratagem>> CODEC = RegistryFileCodec.create(ModRegistries.STRATAGEM, DIRECT_CODEC);

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