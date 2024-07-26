package com.stevekung.stratagems.datagen;

import java.util.concurrent.CompletableFuture;

import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

public class StratagemTagsProvider extends FabricTagProvider<Stratagem>
{
    public StratagemTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider)
    {
        super(output, ModRegistries.STRATAGEM, provider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.getOrCreateTagBuilder(ModConstants.StratagemTag.TNT_REPLENISH).add(Stratagems.TNT);
    }
}