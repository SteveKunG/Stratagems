package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;

public record StratagemActionType(MapCodec<? extends StratagemAction> codec)
{}