package com.stevekung.stratagems.datagen;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;

public class StratagemDataGenerator implements DataGeneratorEntrypoint
{
    private static final List<String> SORT_ORDERS = Util.make(Lists.newArrayList(), list -> Arrays.stream(Stratagem.class.getRecordComponents()).forEach(recordComponent -> list.add(recordComponent.getName())));

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator)
    {
        var pack = dataGenerator.createPack();
        pack.addProvider(DynamicRegistryProvider::new);

        new TestStratagemPackGenerator().onInitializeDataGenerator(dataGenerator);
        new EnderDragonStratagemPackGenerator().onInitializeDataGenerator(dataGenerator);
    }

    @Override
    public void addJsonKeySortOrders(JsonKeySortOrderCallback callback)
    {
        for (var i = 0; i < SORT_ORDERS.size(); i++)
        {
            callback.add(SORT_ORDERS.get(i), i);
        }
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