package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.Stratagem;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public class ModEntityDataSerializers
{
    public static final EntityDataSerializer<Holder<Stratagem>> STRATAGEM = EntityDataSerializer.forValueType(Stratagem.STREAM_CODEC);

    public static void init()
    {
        EntityDataSerializers.registerSerializer(STRATAGEM);
    }
}