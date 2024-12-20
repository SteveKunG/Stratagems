package com.stevekung.stratagems.datagen;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

public class EnderDragonStratagemPackGenerator extends StratagemDataGenerator
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator)
    {
        var pack = dataGenerator.createBuiltinResourcePack(ModConstants.id("ender_dragon_stratagem"));
        //@formatter:off
        var extraProvider = RegistryPatchGenerator.createLookup(dataGenerator.getRegistries(), new RegistrySetBuilder()
                .add(ModRegistries.STRATAGEM, EnderDragonStratagems::bootstrap)
        ).thenApply(RegistrySetBuilder.PatchedRegistries::full);
        //@formatter:on

        pack.addProvider((output, provider) -> new LanguageProvider(output, extraProvider));
        pack.addProvider((output, provider) -> new DynamicRegistryProvider(output, extraProvider));
        pack.addProvider((output, provider) -> forFeaturePack(output, Component.translatable("dataPack.ender_dragon_stratagem.description")));
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder)
    {
        builder.add(ModRegistries.STRATAGEM, EnderDragonStratagems::bootstrap);
    }

    private static class LanguageProvider extends FabricLanguageProvider
    {
        public LanguageProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider)
        {
            super(output, provider);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder builder)
        {
            builder.add("stratagem.stratagems.iron_pickaxe", "Iron Pickaxe");
            builder.add("stratagem.stratagems.iron_axe", "Iron Axe");
            builder.add("stratagem.stratagems.iron_shovel", "Iron Shovel");
            builder.add("stratagem.stratagems.iron_sword", "Iron Sword");
            builder.add("stratagem.stratagems.crafting_table", "Crafting Table");
            builder.add("stratagem.stratagems.furnace", "Furnace");
        }
    }

    private static class DynamicRegistryProvider extends FabricDynamicRegistryProvider
    {
        public DynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider)
        {
            super(output, provider);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries)
        {
            entries.addAll(registries.lookupOrThrow(ModRegistries.STRATAGEM));
        }

        @Override
        public String getName()
        {
            return "Test Stratagem Dynamic Registries";
        }
    }

    private static PackMetadataGenerator forFeaturePack(PackOutput output, Component description)
    {
        var datapackVersion = DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA);
        return new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(description, datapackVersion, Optional.of(new InclusiveRange<>(DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES), datapackVersion))));
    }
}