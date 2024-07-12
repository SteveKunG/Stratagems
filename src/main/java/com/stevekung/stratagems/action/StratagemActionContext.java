package com.stevekung.stratagems.action;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public record StratagemActionContext(ServerLevel level, BlockPos blockPos, RandomSource random)
{}