package com.stevekung.stratagems.datagen;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;

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

public class TestStratagemPackGenerator extends StratagemDataGenerator
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator)
    {
        var pack = dataGenerator.createBuiltinResourcePack(ModConstants.id("stratagem_test_pack"));
        //@formatter:off
        var extraProvider = RegistryPatchGenerator.createLookup(dataGenerator.getRegistries(), new RegistrySetBuilder()
                .add(ModRegistries.STRATAGEM, Stratagems::bootstrapTest)
        ).thenApply(RegistrySetBuilder.PatchedRegistries::full);
        //@formatter:on

        pack.addProvider((output, provider) -> new LanguageProvider(output, extraProvider));
        pack.addProvider((output, provider) -> new StratagemTagsProvider(output, extraProvider));
        pack.addProvider((output, provider) -> new DynamicRegistryProvider(output, extraProvider));
        pack.addProvider((output, provider) -> forFeaturePack(output, Component.translatable("dataPack.stratagem_test_pack.description")));
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder)
    {
        builder.add(ModRegistries.STRATAGEM, Stratagems::bootstrapTest);
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
            builder.add("stratagem.stratagems.bow", "Bow and Arrows");
            builder.add("stratagem.stratagems.supply_chest", "Supply Chest");
            builder.add("stratagem.stratagems.iron_sword", "Iron Sword");
            builder.add("stratagem.stratagems.iron_pickaxe", "Iron Pickaxe");
            builder.add("stratagem.stratagems.block", "Block");
            builder.add("stratagem.stratagems.tnt", "TNT");
            builder.add("stratagem.stratagems.fast_tnt", "Fast TNT");
            builder.add("stratagem.stratagems.long_tnt", "Long TNT");
            builder.add("stratagem.stratagems.tnt_rearm", "TNT Rearm");
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