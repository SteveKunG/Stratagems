package com.stevekung.stratagems.api.references;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public class ModEntityDataSerializers
{
    public static final EntityDataSerializer<Holder<Stratagem>> STRATAGEM = EntityDataSerializer.forValueType(Stratagem.STREAM_CODEC);
    public static final EntityDataSerializer<StratagemInstance.Side> STRATAGEM_SIDE = EntityDataSerializer.forValueType(StratagemInstance.Side.STREAM_CODEC);

    public static void init()
    {
        EntityDataSerializers.registerSerializer(STRATAGEM);
        EntityDataSerializers.registerSerializer(STRATAGEM_SIDE);
    }
}