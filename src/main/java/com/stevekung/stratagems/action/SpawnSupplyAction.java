package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.registry.StratagemActions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

public record SpawnSupplyAction(ResourceKey<LootTable> lootTable) implements StratagemAction
{
    public static final MapCodec<SpawnSupplyAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(SpawnSupplyAction::lootTable)).apply(instance, SpawnSupplyAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActions.SPAWN_SUPPLY;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        var level = context.level();
        var blockPos = context.blockPos();
        level.setBlock(blockPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
        RandomizableContainer.setBlockEntityLootTable(level, context.random(), blockPos, this.lootTable);
    }

    public static Builder spawnSupply(ResourceKey<LootTable> lootTable)
    {
        return () -> new SpawnSupplyAction(lootTable);
    }
}