package com.stevekung.stratagems.api;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void tick(@Nullable MinecraftServer server)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().tick(server, this.player, false);
        }
    }

    @Override
    public boolean canUse(@Nullable MinecraftServer server, Holder<Stratagem> holder, Player player)
    {
        return this.instanceByHolder(holder).canUse(server, player, false);
    }

    @Override
    public void use(@Nullable MinecraftServer server, Holder<Stratagem> holder, Player player)
    {
        this.instanceByHolder(holder).use(server, player, false);
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
    public void reset(@Nullable MinecraftServer server, Holder<Stratagem> holder)
    {
        this.instanceByHolder(holder).reset(server, this.player, false);
    }

    @Override
    public void reset(@Nullable MinecraftServer server)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().reset(server, this.player, false);
        }
    }

    @Override
    public void block(@Nullable MinecraftServer server, boolean unblock)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().block(server, this.player, false, unblock);
        }
    }

    @Override
    public void block(@Nullable MinecraftServer server, Holder<Stratagem> holder, boolean unblock)
    {
        this.instanceByHolder(holder).block(server, this.player, false, unblock);
    }

    @Override
    public void modified(@Nullable MinecraftServer server, StratagemModifier modifier, boolean clear)
    {
        for (var entry : this.instances.entrySet())
        {
            entry.getValue().modified(server, this.player, false, modifier, clear);
        }
    }

    @Override
    public void modified(@Nullable MinecraftServer server, Holder<Stratagem> holder, StratagemModifier modifier, boolean clear)
    {
        this.instanceByHolder(holder).modified(server, this.player, false, modifier, clear);
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

    public void save(ValueOutput valueOutput)
    {
        if (!this.instances.isEmpty())
        {
            var typedOutputList = valueOutput.list(ModConstants.Tag.STRATAGEMS, CompoundTag.CODEC);

            for (var instances : this.listInstances())
            {
                var instanceTag = new CompoundTag();
                instances.save(instanceTag);
                typedOutputList.add(instanceTag);
            }
        }
        valueOutput.putInt(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, this.nextAvailableId);
    }

    public void load(ValueInput valueInput)
    {
        for (var instanceTag : valueInput.listOrEmpty(ModConstants.Tag.STRATAGEMS, CompoundTag.CODEC))
        {
            var instance = StratagemInstance.load(instanceTag, this.player.level());

            if (instance != null)
            {
                this.instances.put(instance.getStratagem(), instance);
            }
        }
        this.nextAvailableId = valueInput.getIntOr(ModConstants.Tag.NEXT_AVAILABLE_STRATAGEM_ID, 0);
    }
}