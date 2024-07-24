package com.stevekung.stratagems.rule;

import com.mojang.serialization.MapCodec;

public record StratagemRuleType(MapCodec<? extends StratagemRule> codec)
{}