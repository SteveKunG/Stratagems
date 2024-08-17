package com.stevekung.stratagems.api.references;

import com.mojang.serialization.Codec;
import com.stevekung.stratagems.api.action.EmptyAction;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionType;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;

public class StratagemActions
{
    private static final Codec<StratagemAction> TYPED_CODEC = ModBuiltInRegistries.STRATAGEM_ACTION_TYPE.byNameCodec().dispatch("type", StratagemAction::getType, StratagemActionType::codec);
    public static final Codec<StratagemAction> DIRECT_CODEC = Codec.lazyInitialized(() -> TYPED_CODEC);
    public static final Codec<Holder<StratagemAction>> CODEC = RegistryFileCodec.create(ModRegistries.STRATAGEM_ACTION, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<StratagemAction>> STREAM_CODEC = ByteBufCodecs.holderRegistry(ModRegistries.STRATAGEM_ACTION);

    public static final StratagemActionType EMPTY = new StratagemActionType(EmptyAction.CODEC);
}