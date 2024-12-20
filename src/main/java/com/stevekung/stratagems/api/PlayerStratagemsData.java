package com.stevekung.stratagems.api;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public class PlayerStratagemsData implements StratagemsData
{
    private final Map<Holder<Stratagem>, StratagemInstance> instances = Maps.newLinkedHashMap();
    private final Player player;
    private int nextAvailableId;

    public PlayerStratagemsData(Player player)
    {
        this.player = player;
    }

    @Override
    public void tick()
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().tick(this.player.getServer(), this.player, false);
        }
    }

    @Override
    public boolean canUse(Holder<Stratagem> holder, Player player)
    {
        return this.instanceByHolder(holder).canUse(this.player.getServer(), player, false);
    }

    @Override
    public void use(Holder<Stratagem> holder, Player player)
    {
        this.instanceByHolder(holder).use(this.player.getServer(), player, false);
    }

    @Override
    public void add(Holder<Stratagem> holder)
    {
        this.add(holder, this.getUniqueId());
    }

    @Override
    public void add(Holder<Stratagem> holder, int id)
    {
        this.add(holder, id, true);
    }

    @Override
    public void add(Holder<Stratagem> holder, int id, boolean shouldDisplay)
    {
        var properties = holder.value().properties();
        var instance = new StratagemInstance(id, holder, properties.inboundDuration(), properties.duration(), properties.cooldown(), properties.cooldown(), properties.maxUse(), StratagemState.READY, StratagemInstance.Side.PLAYER, shouldDisplay, StratagemModifier.NONE);
        this.instances.put(holder, instance);
    }

    @Override
    public void remove(Holder<Stratagem> holder)
    {
        this.instances.remove(holder);
    }

    @Override
    public void reset(Holder<Stratagem> holder)
    {
        this.instanceByHolder(holder).reset(this.player.getServer(), this.player, false);
    }

    @Override
    public void reset()
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().reset(this.player.getServer(), this.player, false);
        }
    }

    @Override
    public void block(boolean unblock)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().block(this.player.getServer(), this.player, false, unblock);
        }
    }

    @Override
    public void block(Holder<Stratagem> holder, boolean unblock)
    {
        this.instanceByHolder(holder).block(this.player.getServer(), this.player, false, unblock);
    }

    @Override
    public void modified(StratagemModifier modifier, boolean clear)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().modified(this.player.getServer(), this.player, false, modifier, clear);
        }
    }

    @Override
    public void modified(Holder<Stratagem> holder, StratagemModifier modifier, boolean clear)
    {
        this.instanceByHolder(holder).modified(this.player.getServer(), this.player, false, modifier, clear);
    }

    @Override
    public void clear()
    {
        this.instances.clear();
        this.nextAvailableId = 0;
    }

    @Override
    public Map<Holder<Stratagem>, StratagemInstance> instances()
    {
        return this.instances;
    }

    @Override
    public Collection<StratagemInstance> listInstances()
    {
        return this.instances.values();
    }

    @Override
    public int size()
    {
        return this.instances.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.instances.isEmpty();
    }

    @Override
    public Stream<StratagemInstance> stream()
    {
        return this.listInstances().stream();
    }

    @Override
    public StratagemInstance instanceByHolder(Holder<Stratagem> holder)
    {
        return this.instances.get(holder);
    }

    private int getUniqueId()
    {
        return this.nextAvailableId += 10;
    }

    public void save(CompoundTag compoundTag)
    {
        if (!this.instances.isEmpty())
        {
            var listTag = new ListTag();

            for (var instances : this.listInstances())
            {
                var instanceTag = new CompoundTag();
                instances.save(instanceTag);
                listTag.add(instanceTag);
            }

            compoundTag.put(ModConstants.Tag.STRATAGEMS, listTag);
        }
        compoundTag.putInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, this.nextAvailableId);
    }

    public void load(CompoundTag compoundTag)
    {
        if (compoundTag.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = compoundTag.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                var instanceTag = listTag.getCompound(i);
                var instance = StratagemInstance.load(instanceTag, this.player.level());

                if (instance != null)
                {
                    this.instances.put(instance.getStratagem(), instance);
                }
            }
        }
        if (compoundTag.contains(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, Tag.TAG_INT))
        {
            this.nextAvailableId = compoundTag.getInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID);
        }
    }
}