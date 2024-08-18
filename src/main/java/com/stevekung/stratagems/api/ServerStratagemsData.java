package com.stevekung.stratagems.api;

import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
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

public class ServerStratagemsData extends SavedData
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STRATAGEM_FILE_ID = "server_stratagems";
    private final Map<Holder<Stratagem>, StratagemInstance> instances = Maps.newLinkedHashMap();
    private final ServerLevel level;
    private int tick;
    private int nextAvailableID;

    public static SavedData.Factory<ServerStratagemsData> factory(ServerLevel level)
    {
        return new SavedData.Factory<>(() -> new ServerStratagemsData(level), (compoundTag, provider) -> load(level, compoundTag), CustomDataFixTypes.SAVED_DATA_STRATAGEMS);
    }

    public ServerStratagemsData(ServerLevel level)
    {
        this.level = level;
        this.nextAvailableID = 1;
        this.setDirty();
    }

    public void tick()
    {
        this.tick++;

        for (var entry : this.instances.entrySet())
        {
            entry.getValue().tick(this.level.getServer(), null);
        }

        if (this.tick % 100 == 0)
        {
            this.setDirty();
        }
    }

    public void use(Holder<Stratagem> holder, Player player)
    {
        var instance = this.instances.get(holder);

        if (instance.canUse(this.level.getServer(), player))
        {
            instance.use(this.level.getServer(), player);
            this.setDirty();
        }
        else
        {
            LOGGER.info("Cannot use {} stratagem because it's in {} state!", instance.stratagem().name().getString(), instance.state);
        }
    }

    public static ServerStratagemsData load(ServerLevel level, CompoundTag tag)
    {
        var data = new ServerStratagemsData(level);
        data.tick = tag.getInt(ModConstants.Tag.TICK);
        data.nextAvailableID = tag.getInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID);

        if (tag.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = tag.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                var instance = StratagemInstance.load(listTag.getCompound(i), level);
                data.instances.put(instance.getStratagem(), instance);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        var listTag = new ListTag();

        for (var instance : this.instances.values())
        {
            var compoundTag = new CompoundTag();
            instance.save(compoundTag);
            listTag.add(compoundTag);
        }

        tag.put(ModConstants.Tag.STRATAGEMS, listTag);
        tag.putInt(ModConstants.Tag.TICK, this.tick);
        tag.putInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, this.nextAvailableID);
        return tag;
    }

    public void add(Holder<Stratagem> holder, Side side)
    {
        var properties = holder.value().properties();
        var instance = new StratagemInstance(this.getUniqueId(), holder, properties.inboundDuration(), properties.duration(), properties.cooldown(), properties.maxUse(), StratagemState.READY, side);
        this.instances.put(holder, instance);
    }

    public void remove(Holder<Stratagem> holder)
    {
        this.instances.remove(holder);
    }

    public void reset(Holder<Stratagem> holder)
    {
        this.instances.get(holder).reset(this.level.getServer(), null);
    }

    public void reset()
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().reset(this.level.getServer(), null);
        }
    }

    public void clear()
    {
        this.instances.clear();
    }

    public Map<Holder<Stratagem>, StratagemInstance> getInstances()
    {
        return this.instances;
    }

    private int getUniqueId()
    {
        return ++this.nextAvailableID;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}