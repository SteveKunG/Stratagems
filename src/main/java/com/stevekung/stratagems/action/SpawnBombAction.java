package com.stevekung.stratagems.action;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.registry.StratagemActions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.PrimedTnt;

public record SpawnBombAction(Optional<Integer> fuse) implements StratagemAction
{
    public static final MapCodec<SpawnBombAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("fuse").forGetter(SpawnBombAction::fuse)
            ).apply(instance, SpawnBombAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActions.SPAWN_BOMB;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        var level = context.level();
        var blockPos = context.blockPos();
        var primedTnt = new PrimedTnt(level, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, context.serverPlayer());
        this.fuse.ifPresent(primedTnt::setFuse);
        level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.addFreshEntity(primedTnt);
    }

    public static Builder spawnBomb()
    {
        return () -> new SpawnBombAction(Optional.empty());
    }

    public static Builder spawnBomb(int fuse)
    {
        return () -> new SpawnBombAction(Optional.of(fuse));
    }
}