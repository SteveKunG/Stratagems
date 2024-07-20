package com.stevekung.stratagems;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

public record StratagemProperties(int incomingDuration, Optional<Integer> duration, int nextUseCooldown, int beamColor)
{
    public static final Codec<StratagemProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("incoming_duration").forGetter(StratagemProperties::incomingDuration),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("duration").forGetter(StratagemProperties::duration),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("next_use_cooldown").forGetter(StratagemProperties::nextUseCooldown),
            Codec.INT.fieldOf("beam_color").forGetter(StratagemProperties::beamColor)
            ).apply(instance, StratagemProperties::new));
}