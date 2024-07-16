package com.stevekung.stratagems;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StratagemsData extends SavedData
{
    private static final String STRATAGEM_FILE_ID = "stratagems";
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
        var raids = new StratagemsData(level);
        raids.tick = tag.getInt("Tick");
        //        var listTag = tag.getList("Raids", 10);
        //
        //        for (var i = 0; i < listTag.size(); i++) {
        //            var compoundTag = listTag.getCompound(i);
        //            var raid = new Raid(level, compoundTag);
        //            raids.raidMap.put(raid.getId(), raid);
        //        }

        return raids;
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider registries)
    {
        tag.putInt("Tick", this.tick);
        //        var listTag = new ListTag();
        //
        //        for (var raid : this.raidMap.values()) {
        //            var compoundTag = new CompoundTag();
        //            raid.save(compoundTag);
        //            listTag.add(compoundTag);
        //        }
        //
        //        tag.put("Raids", listTag);
        return tag;
    }

    public static String getFileId()
    {
        return STRATAGEM_FILE_ID;
    }
}
