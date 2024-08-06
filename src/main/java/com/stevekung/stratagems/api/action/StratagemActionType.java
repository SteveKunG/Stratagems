package com.stevekung.stratagems.api.action;

import com.mojang.serialization.MapCodec;

public record StratagemActionType(MapCodec<? extends StratagemAction> codec)
{}