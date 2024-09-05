package com.stevekung.stratagems.api.references;

import com.mojang.serialization.Codec;
import com.stevekung.stratagems.api.action.EmptyAction;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionType;

public class StratagemActions
{
    private static final Codec<StratagemAction> TYPED_CODEC = ModBuiltInRegistries.STRATAGEM_ACTION_TYPE.byNameCodec().dispatch("type", StratagemAction::getType, StratagemActionType::codec);
    public static final Codec<StratagemAction> DIRECT_CODEC = Codec.lazyInitialized(() -> TYPED_CODEC);

    public static final StratagemActionType EMPTY = new StratagemActionType(EmptyAction.CODEC);
}