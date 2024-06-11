package com.stevekung.stratagems.datagen;

import java.util.concurrent.CompletableFuture;

import com.stevekung.stratagems.StratagemsMod;
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
        pack.addProvider(DynamicRegistryProvider::new);
    }

    @Override
    public void addJsonKeySortOrders(JsonKeySortOrderCallback callback)
    {
        callback.add("code", 0);
        callback.add("name", 1);
        callback.add("icon", 2);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder)
    {
        builder.add(StratagemsMod.STRATAGEM_KEY, Stratagems::bootstrap);
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
            entries.addAll(registries.lookupOrThrow(StratagemsMod.STRATAGEM_KEY));
        }

        @Override
        public String getName()
        {
            return "Stratagem Dynamic Registries";
        }
    }
}