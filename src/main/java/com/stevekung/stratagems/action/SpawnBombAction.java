package com.stevekung.stratagems.action;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.registry.StratagemActionTypes;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.state.BlockState;

public record SpawnBombAction(Optional<Integer> fuse, Optional<BlockState> blockState) implements StratagemAction
{
    public static final MapCodec<SpawnBombAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("fuse").forGetter(SpawnBombAction::fuse),
            BlockState.CODEC.optionalFieldOf("block_state").forGetter(SpawnBombAction::blockState)
            ).apply(instance, SpawnBombAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActionTypes.SPAWN_BOMB;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        var level = context.level();
        var blockPos = context.blockPos();
        var primedTnt = new PrimedTnt(level, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, context.serverPlayer());
        this.fuse.ifPresent(primedTnt::setFuse);
        this.blockState.ifPresent(primedTnt::setBlockState);
        level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.addFreshEntity(primedTnt);
    }

    public static Builder spawnBomb()
    {
        return () -> new SpawnBombAction(Optional.empty(), Optional.empty());
    }

    public static Builder spawnBomb(int fuse)
    {
        return () -> new SpawnBombAction(Optional.of(fuse), Optional.empty());
    }

    public static Builder spawnBomb(int fuse, BlockState blockState)
    {
        return () -> new SpawnBombAction(Optional.of(fuse), Optional.of(blockState));
    }
}