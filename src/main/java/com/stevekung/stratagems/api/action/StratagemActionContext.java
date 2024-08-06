package com.stevekung.stratagems.api.action;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public record StratagemActionContext(ServerPlayer serverPlayer, ServerLevel level, BlockPos blockPos, RandomSource random)
{}