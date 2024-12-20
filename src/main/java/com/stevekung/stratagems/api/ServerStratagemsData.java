package com.stevekung.stratagems.api;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.stevekung.stratagems.api.StratagemInstance.Side;
import com.stevekung.stratagems.api.util.CustomDataFixTypes;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class ServerStratagemsData extends SavedData implements StratagemsData
{
    private static final String STRATAGEM_FILE_ID = "server_stratagems";
    private final Map<Holder<Stratagem>, StratagemInstance> instances = Maps.newLinkedHashMap();
    private final ServerLevel level;
    private int tick;
    private int nextAvailableId;

    public static SavedData.Factory<ServerStratagemsData> factory(ServerLevel level)
    {
        return new SavedData.Factory<>(() -> new ServerStratagemsData(level), (compoundTag, provider) -> load(level, compoundTag), CustomDataFixTypes.SAVED_DATA_STRATAGEMS);
    }

    public ServerStratagemsData(ServerLevel level)
    {
        this.level = level;
        this.setDirty();
    }

    @Override
    public void tick()
    {
        this.tick++;

        for (var entry : this.instances.entrySet())
        {
            entry.getValue().tick(this.level.getServer(), null, true);
        }

        if (this.tick % 100 == 0)
        {
            this.setDirty();
        }
    }

    @Override
    public boolean canUse(Holder<Stratagem> holder, Player player)
    {
        return this.instanceByHolder(holder).canUse(this.level.getServer(), player, true);
    }

    @Override
    public void use(Holder<Stratagem> holder, Player player)
    {
        this.instanceByHolder(holder).use(this.level.getServer(), player, true);
        this.setDirty();
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
        var instance = new StratagemInstance(id, holder, properties.inboundDuration(), properties.duration(), properties.cooldown(), properties.cooldown(), properties.maxUse(), StratagemState.READY, Side.SERVER, shouldDisplay, StratagemModifier.NONE);
        this.instances.put(holder, instance);
        this.setDirty();
    }

    @Override
    public void remove(Holder<Stratagem> holder)
    {
        this.instances.remove(holder);
        this.setDirty();
    }

    @Override
    public void reset(Holder<Stratagem> holder)
    {
        this.instanceByHolder(holder).reset(this.level.getServer(), null, true);
        this.setDirty();
    }

    @Override
    public void reset()
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().reset(this.level.getServer(), null, true);
        }
        this.setDirty();
    }

    @Override
    public void block(boolean unblock)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().block(this.level.getServer(), null, true, unblock);
        }
        this.setDirty();
    }

    @Override
    public void block(Holder<Stratagem> holder, boolean unblock)
    {
        this.instanceByHolder(holder).block(this.level.getServer(), null, true, unblock);
        this.setDirty();
    }

    @Override
    public void modified(StratagemModifier modifier, boolean clear)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().modified(this.level.getServer(), null, true, modifier, clear);
        }
    }

    @Override
    public void modified(Holder<Stratagem> holder, StratagemModifier modifier, boolean clear)
    {
        this.instanceByHolder(holder).modified(this.level.getServer(), null, true, modifier, clear);
    }

    @Override
    public void clear()
    {
        this.instances.clear();
        this.nextAvailableId = 0;
        this.setDirty();
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

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider)
    {
        var listTag = new ListTag();

        for (var instance : this.listInstances())
        {
            var instanceTag = new CompoundTag();
            instance.save(instanceTag);
            listTag.add(instanceTag);
        }

        compoundTag.put(ModConstants.Tag.STRATAGEMS, listTag);
        compoundTag.putInt(ModConstants.Tag.TICK, this.tick);
        compoundTag.putInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, this.nextAvailableId);
        return compoundTag;
    }

    public static ServerStratagemsData load(ServerLevel level, CompoundTag compoundTag)
    {
        var data = new ServerStratagemsData(level);
        data.tick = compoundTag.getInt(ModConstants.Tag.TICK);
        data.nextAvailableId = compoundTag.getInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID);

        if (compoundTag.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = compoundTag.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                var instance = StratagemInstance.load(listTag.getCompound(i), level);

                if (instance != null)
                {
                    data.instances.put(instance.getStratagem(), instance);
                }
            }
        }
        return data;
    }

    private int getUniqueId()
    {
        return this.nextAvailableId += 10;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}