package com.stevekung.stratagems;

import java.util.List;

import com.google.common.collect.Lists;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StratagemsData extends SavedData
{
    private static final String STRATAGEM_FILE_ID = "stratagems";
    private final List<ResourceKey<Stratagem>> stratagemList = Util.make(Lists.newArrayList(), list ->
    {
        list.add(Stratagems.REINFORCE);
        list.add(Stratagems.SUPPLY_CHEST);
        list.add(Stratagems.BLOCK);
    });
    private final ServerLevel level;
    private int tick;

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

        if (this.tick % 200 == 0)
        {
            this.setDirty();
        }
    }

    public static StratagemsData load(ServerLevel level, CompoundTag tag)
    {
        var stratagems = new StratagemsData(level);
        var listTag = tag.getList("stratagems", Tag.TAG_COMPOUND);

        for (var i = 0; i < listTag.size(); i++)
        {
            var compoundTag = listTag.getCompound(i);
            stratagems.stratagemList.add(ResourceKey.create(ModRegistries.STRATAGEM, ResourceLocation.parse(compoundTag.getString("stratagem"))));
            stratagems.tick = tag.getInt("tick");
        }

        return stratagems;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        var listTag = new ListTag();

        for (var stratagem : this.stratagemList)
        {
            var compoundTag = new CompoundTag();
            compoundTag.putString("stratagem", stratagem.location().toString());
            compoundTag.putInt("tick", this.tick);
            listTag.add(compoundTag);
        }

        tag.put("stratagems", listTag);
        return tag;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}