package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.Stratagem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries
{
    public static final ResourceKey<Registry<Stratagem>> STRATAGEM = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("stratagem"));
}