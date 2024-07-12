package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.registry.StratagemActions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record SpawnItemAction(ItemStack itemStack) implements StratagemAction
{
    public static final MapCodec<SpawnItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ItemStack.CODEC.fieldOf("item").forGetter(SpawnItemAction::itemStack)).apply(instance, SpawnItemAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActions.SPAWN_ITEM;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        Block.popResource(context.level(), context.blockPos(), this.itemStack);
    }

    public static Builder spawnItem(ItemStack itemStack)
    {
        return () -> new SpawnItemAction(itemStack);
    }
}