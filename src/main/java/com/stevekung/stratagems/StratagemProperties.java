package com.stevekung.stratagems;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record StratagemProperties(int inboundDuration, Optional<Integer> duration, int cooldown, Optional<Integer> remainingUse, int beamColor, Optional<Boolean> canDepleted, Optional<Boolean> needThrow, Optional<StratagemReplenish> replenish)
{
    public static final Codec<StratagemProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("inbound_duration").forGetter(StratagemProperties::inboundDuration),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("duration").forGetter(StratagemProperties::duration),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown").forGetter(StratagemProperties::cooldown),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("remaining_use").forGetter(StratagemProperties::remainingUse),
            Codec.INT.fieldOf("beam_color").forGetter(StratagemProperties::beamColor),
            Codec.BOOL.optionalFieldOf("can_depleted").forGetter(StratagemProperties::canDepleted),
            Codec.BOOL.optionalFieldOf("need_throw").forGetter(StratagemProperties::needThrow),
            StratagemReplenish.CODEC.optionalFieldOf("replenish").forGetter(StratagemProperties::replenish)
            ).apply(instance, StratagemProperties::new));

    public static StratagemProperties simple(int inboundDuration, int cooldown, int beamColor)
    {
        return new StratagemProperties(inboundDuration, Optional.empty(), cooldown, Optional.empty(), beamColor, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static StratagemProperties withReplenish(int inboundDuration, int cooldown, int remainingUse, int beamColor, StratagemReplenish replenish)
    {
        return new StratagemProperties(inboundDuration, Optional.empty(), cooldown, Optional.of(remainingUse), beamColor, Optional.of(true), Optional.empty(), Optional.of(replenish));
    }

    public static StratagemProperties withDepleted(int inboundDuration, int cooldown, int remainingUse, int beamColor)
    {
        return new StratagemProperties(inboundDuration, Optional.empty(), cooldown, Optional.of(remainingUse), beamColor, Optional.empty(), Optional.empty(), Optional.empty());
    }
}