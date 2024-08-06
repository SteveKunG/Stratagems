package com.stevekung.stratagems.api.rule;

import com.mojang.serialization.MapCodec;

public record StratagemRuleType(MapCodec<? extends StratagemRule> codec)
{}