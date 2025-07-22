package com.stevekung.stratagems.api.references;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataSerializer;

public class ModEntityDataSerializers
{
    public static final EntityDataSerializer<Holder<Stratagem>> STRATAGEM = EntityDataSerializer.forValueType(Stratagem.STREAM_CODEC);
    public static final EntityDataSerializer<StratagemInstance.Side> STRATAGEM_SIDE = EntityDataSerializer.forValueType(StratagemInstance.Side.STREAM_CODEC);

    public static void init()
    {
        FabricTrackedDataRegistry.register(ModConstants.id("stratagem"), STRATAGEM);
        FabricTrackedDataRegistry.register(ModConstants.id("stratagem_side"), STRATAGEM_SIDE);
    }
}