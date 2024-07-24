package com.stevekung.stratagems;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stevekung.stratagems.registry.Stratagems;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StratagemsData extends SavedData
{
    private static final String STRATAGEM_FILE_ID = "stratagems";
    private final List<StratagemsTicker> stratagemList = Lists.newArrayList();
    private final ServerLevel level;
    private int tick;

    private static final Set<ResourceKey<Stratagem>> DEFAULT_STRATAGEMS = Util.make(Sets.newLinkedHashSet(), set ->
    {
        set.add(Stratagems.REINFORCE);
        set.add(Stratagems.SUPPLY_CHEST);
        set.add(Stratagems.BLOCK);
    });

    public static SavedData.Factory<StratagemsData> factory(ServerLevel level)
    {
        return new SavedData.Factory<>(() -> new StratagemsData(level), (compoundTag, provider) -> load(level, compoundTag), CustomDataFixTypes.SAVED_DATA_STRATAGEMS);
    }

    public StratagemsData(ServerLevel level)
    {
        this.level = level;
        this.setDirty();
    }

    public void tick()
    {
        this.tick++;
        this.stratagemList.forEach(StratagemsTicker::tick);

        if (this.tick % 100 == 0)
        {
            this.setDirty();
        }
    }

    public void useStratagem(ResourceKey<Stratagem> resourceKey)
    {
        this.stratagemList.stream().filter(ticker -> ticker.getStratagem().is(resourceKey)).forEach(StratagemsTicker::useStratagem);
    }

    public static StratagemsData load(ServerLevel level, CompoundTag tag)
    {
        var stratagems = new StratagemsData(level);
        stratagems.tick = tag.getInt(ModConstants.Tag.TICK);

        var listTag = tag.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

        for (var i = 0; i < listTag.size(); i++)
        {
            var compoundTag = listTag.getCompound(i);
            stratagems.stratagemList.add(new StratagemsTicker(level, compoundTag));
        }

        return stratagems;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        var listTag = new ListTag();

        if (this.stratagemList.isEmpty())
        {
            DEFAULT_STRATAGEMS.forEach(resourceKey ->
            {
                var compoundTag = new CompoundTag();
                compoundTag.putString(ModConstants.Tag.STRATAGEM, resourceKey.location().toString());
                this.stratagemList.add(new StratagemsTicker(this.level, compoundTag));
            });
        }

        for (var stratagem : this.stratagemList)
        {
            var compoundTag = new CompoundTag();
            stratagem.save(compoundTag);
            listTag.add(compoundTag);
        }

        tag.put(ModConstants.Tag.STRATAGEMS, listTag);
        tag.putInt(ModConstants.Tag.TICK, this.tick);
        return tag;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}