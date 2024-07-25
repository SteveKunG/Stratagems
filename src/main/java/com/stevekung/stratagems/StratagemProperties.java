package com.stevekung.stratagems;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

public record StratagemProperties(int incomingDuration, Optional<Integer> duration, int cooldown, Optional<Integer> remainingUse, int beamColor, Optional<Boolean> canDepleted)
{
    public static final Codec<StratagemProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("incoming_duration").forGetter(StratagemProperties::incomingDuration),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("duration").forGetter(StratagemProperties::duration),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown").forGetter(StratagemProperties::cooldown),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("remaining_use").forGetter(StratagemProperties::remainingUse),
            Codec.INT.fieldOf("beam_color").forGetter(StratagemProperties::beamColor),
            Codec.BOOL.optionalFieldOf("can_depleted").forGetter(StratagemProperties::canDepleted)
            ).apply(instance, StratagemProperties::new));
}