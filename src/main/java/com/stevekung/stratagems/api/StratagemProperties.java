package com.stevekung.stratagems.api;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

public record StratagemProperties(int inboundDuration, int duration, int cooldown, int maxUse, int beamColor, boolean canDepleted, boolean needThrow, Optional<StratagemReplenish> replenish)
{
    public static final Codec<StratagemProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("inbound_duration").forGetter(StratagemProperties::inboundDuration),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("duration", -1).forGetter(StratagemProperties::duration),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown").forGetter(StratagemProperties::cooldown),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_use", -1).forGetter(StratagemProperties::maxUse),
            Codec.INT.fieldOf("beam_color").forGetter(StratagemProperties::beamColor),
            Codec.BOOL.optionalFieldOf("can_depleted", false).forGetter(StratagemProperties::canDepleted),
            Codec.BOOL.optionalFieldOf("need_throw", true).forGetter(StratagemProperties::needThrow),
            StratagemReplenish.CODEC.optionalFieldOf("replenish").forGetter(StratagemProperties::replenish)
            ).apply(instance, StratagemProperties::new));

    public static StratagemProperties simple(int inboundDuration, int cooldown, int beamColor)
    {
        return new StratagemProperties(inboundDuration, -1, cooldown, -1, beamColor, false, true, Optional.empty());
    }

    public static StratagemProperties withReplenish(int inboundDuration, int cooldown, int maxUse, int beamColor, StratagemReplenish replenish)
    {
        return new StratagemProperties(inboundDuration, -1, cooldown, maxUse, beamColor, true, true, Optional.of(replenish));
    }

    public static StratagemProperties withDepleted(int inboundDuration, int cooldown, int maxUse, int beamColor)
    {
        return new StratagemProperties(inboundDuration, -1, cooldown, maxUse, beamColor, false, true, Optional.empty());
    }
}