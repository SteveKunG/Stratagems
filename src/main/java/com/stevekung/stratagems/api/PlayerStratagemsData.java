package com.stevekung.stratagems.api;

import java.util.Map;

import com.google.common.collect.Maps;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public class PlayerStratagemsData implements StratagemsData
{
    private final Map<Holder<Stratagem>, StratagemInstance> instances = Maps.newLinkedHashMap();
    private final Player player;
    private int nextAvailableId = 1;

    public PlayerStratagemsData(Player player)
    {
        this.player = player;
    }

    @Override
    public void tick()
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().tick(this.player.getServer(), this.player);
        }
    }

    @Override
    public void use(Holder<Stratagem> holder, Player player)
    {
        this.instances.get(holder).use(this.player.getServer(), player);
    }

    @Override
    public void add(Holder<Stratagem> holder)
    {
        this.instances.put(holder, StratagemUtils.createInstanceForPlayer(holder, this.getUniqueId()));
    }

    @Override
    public void remove(Holder<Stratagem> holder)
    {
        this.instances.remove(holder);
    }

    @Override
    public void reset(Holder<Stratagem> holder)
    {
        this.instances.get(holder).reset(this.player.getServer(), this.player);
    }

    @Override
    public void reset()
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().reset(this.player.getServer(), this.player);
        }
    }

    @Override
    public void clear()
    {
        this.instances.clear();
    }

    @Override
    public Map<Holder<Stratagem>, StratagemInstance> instances()
    {
        return this.instances;
    }

    @Override
    public StratagemInstance instanceByHolder(Holder<Stratagem> holder)
    {
        return this.instances.get(holder);
    }

    public void load(CompoundTag compound)
    {
        if (!this.instances.isEmpty())
        {
            var listTag = new ListTag();

            for (var instances : this.instances.values())
            {
                var compoundTag = new CompoundTag();
                instances.save(compoundTag);
                listTag.add(compoundTag);
            }

            compound.put(ModConstants.Tag.STRATAGEMS, listTag);
        }
        compound.putInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, this.nextAvailableId);
    }

    public void save(CompoundTag compound)
    {
        if (compound.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = compound.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                var compoundTag = listTag.getCompound(i);
                var instance = StratagemInstance.load(compoundTag, this.player.level());
                this.instances.put(instance.getStratagem(), instance);
            }
        }
        if (compound.contains(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, Tag.TAG_INT))
        {
            this.nextAvailableId = compound.getInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID);
        }
    }

    private int getUniqueId()
    {
        return ++this.nextAvailableId;
    }
}