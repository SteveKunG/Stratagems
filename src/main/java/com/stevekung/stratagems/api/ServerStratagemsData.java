package com.stevekung.stratagems.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.StratagemInstance.Side;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.util.CustomDataFixTypes;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ServerStratagemsData extends SavedData implements StratagemsData
{
    public static final Codec<ServerStratagemsData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            StratagemWithId.CODEC.listOf().optionalFieldOf("stratagems", List.of()).forGetter(data -> data.instances.entrySet().stream().map(StratagemWithId::from).toList()),
                            Codec.INT.fieldOf("next_id").forGetter(data -> data.nextAvailableId),
                            Codec.INT.fieldOf("tick").forGetter(data -> data.tick))
                    .apply(instance, ServerStratagemsData::new));

    public static final SavedDataType<ServerStratagemsData> TYPE = new SavedDataType<>("server_stratagems",
            context -> new ServerStratagemsData(context.levelOrThrow()), context -> CODEC, CustomDataFixTypes.SAVED_DATA_STRATAGEMS);

    private final Map<Holder<Stratagem>, StratagemInstance> instances = Maps.newLinkedHashMap();
    private ServerLevel level;
    private int nextAvailableId;
    private int tick;

    public ServerStratagemsData()
    {
        this.setDirty();
    }

    public ServerStratagemsData(ServerLevel level)
    {
        this();
        this.level = level;
    }

    public ServerStratagemsData(List<StratagemWithId> list, int nextAvailableId, int tick)
    {
        for (var stratagemWithId : list)
        {
            this.instances.put(stratagemWithId.stratagem, stratagemWithId.instance);
        }
        this.nextAvailableId = nextAvailableId;
        this.tick = tick;
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

    public static ServerStratagemsData load(CompoundTag compoundTag)
    {
        return CODEC.parse(NbtOps.INSTANCE, compoundTag).resultOrPartial().orElseGet(ServerStratagemsData::new);
    }

    private int getUniqueId()
    {
        return this.nextAvailableId += 10;
    }

    public record StratagemWithId(Holder<Stratagem> stratagem, StratagemInstance instance)
    {
        public static final Codec<StratagemWithId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        RegistryFixedCodec.create(ModRegistries.STRATAGEM).fieldOf("stratagem").forGetter(StratagemWithId::stratagem),
                        StratagemInstance.MAP_CODEC.forGetter(StratagemWithId::instance))
                .apply(instance, StratagemWithId::new));

        public static StratagemWithId from(Map.Entry<Holder<Stratagem>, StratagemInstance> entry)
        {
            return new StratagemWithId(entry.getKey(), entry.getValue());
        }
    }
}