package com.stevekung.stratagems.action;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.registry.StratagemActions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record SpawnItemAction(ItemStack primary, Optional<ItemStack> secondary) implements StratagemAction
{
    public static final MapCodec<SpawnItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("primary").forGetter(SpawnItemAction::primary),
            ItemStack.CODEC.optionalFieldOf("secondary").forGetter(SpawnItemAction::secondary)
            ).apply(instance, SpawnItemAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActions.SPAWN_ITEM;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        Block.popResource(context.level(), context.blockPos(), this.primary);
        this.secondary.ifPresent(secondary -> Block.popResource(context.level(), context.blockPos(), this.secondary.get()));
    }

    public static Builder spawnItem(ItemStack itemStack)
    {
        return () -> new SpawnItemAction(itemStack, Optional.empty());
    }

    public static Builder spawnItems(ItemStack primary, ItemStack secondary)
    {
        return () -> new SpawnItemAction(primary, Optional.of(secondary));
    }
}