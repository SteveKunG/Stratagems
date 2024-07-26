package com.stevekung.stratagems.datagen;

import java.util.concurrent.CompletableFuture;

import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;

public class StratagemDataGenerator implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator)
    {
        var pack = dataGenerator.createPack();
        pack.addProvider(StratagemTagsProvider::new);
        pack.addProvider(DynamicRegistryProvider::new);
    }

    @Override
    public void addJsonKeySortOrders(JsonKeySortOrderCallback callback)
    {
        callback.add("code", 0);
        callback.add("name", 1);
        callback.add("action", 2);
        callback.add("properties", 3);
        callback.add("icon", 4);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder)
    {
        builder.add(ModRegistries.STRATAGEM, Stratagems::bootstrap);
    }

    private static class DynamicRegistryProvider extends FabricDynamicRegistryProvider
    {
        public DynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
        {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries)
        {
            entries.addAll(registries.lookupOrThrow(ModRegistries.STRATAGEM));
        }

        @Override
        public String getName()
        {
            return "Stratagem Dynamic Registries";
        }
    }
}