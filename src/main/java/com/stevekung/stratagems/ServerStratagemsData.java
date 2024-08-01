package com.stevekung.stratagems;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;
import com.stevekung.stratagems.util.CustomDataFixTypes;
import com.stevekung.stratagems.util.StratagemUtils;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class ServerStratagemsData extends SavedData
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STRATAGEM_FILE_ID = "server_stratagems";
    private final List<StratagemInstance> stratagemInstances = Lists.newCopyOnWriteArrayList();
    private final ServerLevel level;
    private int tick;

    public static final Set<ResourceKey<Stratagem>> DEFAULT_STRATAGEMS = Util.make(Sets.newLinkedHashSet(), set ->
    {
        set.add(Stratagems.REINFORCE);
        set.add(Stratagems.SUPPLY_CHEST);
        set.add(Stratagems.BLOCK);
    });

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
        this.stratagemInstances.forEach(instance -> instance.tick(this.level.getServer(), null));

        if (this.tick % 100 == 0)
        {
            this.setDirty();
        }
    }

    public void use(ResourceKey<Stratagem> resourceKey, Player player)
    {
        this.stratagemInstances.stream().filter(entry -> entry.getStratagem().value().id().equals(resourceKey)).findFirst().ifPresent(entry ->
        {
            if (entry.canUse(this.level.getServer(), player))
            {
                entry.use(this.level.getServer(), player);
                this.setDirty();
            }
            else
            {
                LOGGER.info("Cannot use {} stratagem because it's in {} state!", entry.stratagem().name().getString(), entry.state);
            }
        });
    }

    public static ServerStratagemsData load(ServerLevel level, CompoundTag tag)
    {
        var stratagems = new ServerStratagemsData(level);
        stratagems.tick = tag.getInt(ModConstants.Tag.TICK);

        if (tag.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = tag.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                var compoundTag = listTag.getCompound(i);
                var stratagemInstance = StratagemInstance.load(compoundTag);

                if (stratagemInstance != null)
                {
                    stratagems.stratagemInstances.add(stratagemInstance);
                }
            }
        }

        return stratagems;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        var listTag = new ListTag();

        if (this.stratagemInstances.isEmpty())
        {
            this.addDefaultStratagems(provider);
        }

        for (var stratagemInstance : this.stratagemInstances)
        {
            listTag.add(stratagemInstance.save());
        }

        tag.put(ModConstants.Tag.STRATAGEMS, listTag);
        tag.putInt(ModConstants.Tag.TICK, this.tick);
        return tag;
    }

    private void addDefaultStratagems(HolderLookup.Provider provider)
    {
        DEFAULT_STRATAGEMS.forEach(resourceKey -> this.stratagemInstances.add(StratagemUtils.createInstanceWithDefaultValue(provider.lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey))));
    }

    public void add(StratagemInstance instance)
    {
        this.stratagemInstances.add(instance);
    }

    public void remove(Holder<Stratagem> stratagemHolder)
    {
        this.stratagemInstances.removeIf(entry -> entry.getStratagem() == stratagemHolder);
    }

    public void reset(Holder<Stratagem> stratagemHolder)
    {
        this.stratagemInstances.stream().filter(entry -> entry.getStratagem() == stratagemHolder).forEach(instance -> instance.reset(this.level.getServer(), null));
    }

    public void clear()
    {
        this.stratagemInstances.clear();
        this.addDefaultStratagems(this.level.registryAccess());
    }

    public List<StratagemInstance> getStratagemInstances()
    {
        return this.stratagemInstances;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}