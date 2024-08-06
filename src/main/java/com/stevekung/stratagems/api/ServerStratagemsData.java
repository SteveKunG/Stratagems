package com.stevekung.stratagems.api;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
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
    private final List<StratagemInstance> instances = Lists.newCopyOnWriteArrayList();
    private final ServerLevel level;
    private int tick;

    public static SavedData.Factory<ServerStratagemsData> factory(ServerLevel level)
    {
        return new SavedData.Factory<>(() -> new ServerStratagemsData(level), (compoundTag, provider) -> load(level, compoundTag), CustomDataFixTypes.SAVED_DATA_STRATAGEMS);
    }

    public ServerStratagemsData(ServerLevel level)
    {
        this.level = level;
        this.setDirty();
    }

    public void tick()
    {
        this.tick++;
        this.instances.forEach(instance -> instance.tick(this.level.getServer(), null));

        if (this.tick % 100 == 0)
        {
            this.setDirty();
        }
    }

    public void use(Holder<Stratagem> holder, Player player)
    {
        this.instances.stream().filter(instance -> instance.getStratagem() == holder).findFirst().ifPresent(instance ->
        {
            if (instance.canUse(this.level.getServer(), player))
            {
                instance.use(this.level.getServer(), player);
                this.setDirty();
            }
            else
            {
                LOGGER.info("Cannot use {} stratagem because it's in {} state!", instance.stratagem().name().getString(), instance.state);
            }
        });
    }

    public static ServerStratagemsData load(ServerLevel level, CompoundTag tag)
    {
        var data = new ServerStratagemsData(level);
        data.tick = tag.getInt(ModConstants.Tag.TICK);

        if (tag.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = tag.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                data.instances.add(StratagemInstance.load(listTag.getCompound(i), level));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        var listTag = new ListTag();

        for (var instance : this.instances)
        {
            var compoundTag = new CompoundTag();
            instance.save(compoundTag);
            listTag.add(compoundTag);
        }

        tag.put(ModConstants.Tag.STRATAGEMS, listTag);
        tag.putInt(ModConstants.Tag.TICK, this.tick);
        return tag;
    }

    public void add(StratagemInstance instance)
    {
        this.instances.add(instance);
    }

    public void remove(Holder<Stratagem> holder)
    {
        this.instances.removeIf(instance -> instance.getStratagem() == holder);
    }

    public void reset(Holder<Stratagem> holder)
    {
        this.instances.stream().filter(instance -> instance.getStratagem() == holder).forEach(instance -> instance.reset(this.level.getServer(), null));
    }

    public void reset()
    {
        this.instances.forEach(instance -> instance.reset(this.level.getServer(), null));
    }

    public void clear()
    {
        this.instances.clear();
    }

    public List<StratagemInstance> getInstances()
    {
        return this.instances;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}