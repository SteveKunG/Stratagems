package com.stevekung.stratagems;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.StratagemSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StratagemsMod implements ModInitializer
{
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "stratagems";

    @Override
    public void onInitialize()
    {
        StratagemSounds.init();

        DynamicRegistries.registerSynced(ModRegistries.STRATAGEM, Stratagem.DIRECT_CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);
        DynamicRegistrySetupCallback.EVENT.register(registryView -> addListenerForDynamic(registryView, ModRegistries.STRATAGEM));
    }

    public static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void addListenerForDynamic(DynamicRegistryView registryView, ResourceKey<? extends Registry<?>> key)
    {
        registryView.registerEntryAdded(key, (rawId, id, object) -> LOGGER.info("Loaded entry of {}: {} = {}", key, id, object));
    }
}