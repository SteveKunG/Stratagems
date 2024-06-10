package com.stevekung.stratagems;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.registry.StratagemSounds;
import com.stevekung.stratagems.registry.Stratagems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StratagemsMod implements ModInitializer
{
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "stratagems";

    public static final ResourceKey<Registry<Stratagem>> STRATAGEM_KEY = ResourceKey.createRegistryKey(id("stratagem"));
    public static final Registry<Stratagem> STRATAGEM_REGISTRY = BuiltInRegistries.registerSimple(STRATAGEM_KEY, registry -> Stratagems.BLOCK);

    @Override
    public void onInitialize()
    {
        Stratagems.init();
        StratagemSounds.init();
    }

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }
}