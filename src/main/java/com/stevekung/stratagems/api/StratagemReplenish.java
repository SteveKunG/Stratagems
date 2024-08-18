package com.stevekung.stratagems.api;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public record StratagemReplenish(Optional<ResourceKey<Stratagem>> replenisher, String category, Optional<HolderSet<Stratagem>> toReplenish, Optional<SoundEvent> replenishSound)
{
    public static final Codec<StratagemReplenish> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(ModRegistries.STRATAGEM).optionalFieldOf("replenisher").forGetter(StratagemReplenish::replenisher),
            Codec.STRING.fieldOf("category").forGetter(StratagemReplenish::category),
            RegistryCodecs.homogeneousList(ModRegistries.STRATAGEM).optionalFieldOf("to_replenish").forGetter(StratagemReplenish::toReplenish),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("replenish_sound").forGetter(StratagemReplenish::replenishSound)
            ).apply(instance, StratagemReplenish::new));
}