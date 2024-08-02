package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;

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