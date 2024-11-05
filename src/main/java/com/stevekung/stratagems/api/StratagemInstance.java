package com.stevekung.stratagems.api;

import java.util.Locale;
import java.util.Optional;
import java.util.function.IntFunction;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.rule.StratagemRule;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StratagemInstance implements Comparable<StratagemInstance>
{
    private final Holder<Stratagem> stratagem;
    public final int id;
    public int inboundDuration;
    public int duration;
    public int cooldown;
    public int lastMaxCooldown; // Used only on the client
    public int maxUse;
    public StratagemState state;
    public final Side side;
    public final boolean shouldDisplay;

    public StratagemInstance(int id, Holder<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int lastMaxCooldown, int maxUse, StratagemState state, Side side, boolean shouldDisplay)
    {
        this.id = id;
        this.stratagem = stratagem;
        this.inboundDuration = inboundDuration;
        this.duration = duration;
        this.cooldown = cooldown;
        this.lastMaxCooldown = lastMaxCooldown;
        this.maxUse = maxUse;
        this.state = state;
        this.side = side;
        this.shouldDisplay = shouldDisplay;
    }

    public void save(CompoundTag compoundTag)
    {
        if (this.inboundDuration > 0)
        {
            compoundTag.putInt(ModConstants.Tag.INBOUND_DURATION, this.inboundDuration);
        }

        if (this.duration > 0)
        {
            compoundTag.putInt(ModConstants.Tag.DURATION, this.duration);
        }

        compoundTag.putInt(ModConstants.Tag.COOLDOWN, this.cooldown);
        compoundTag.putInt(ModConstants.Tag.LAST_MAX_COOLDOWN, this.lastMaxCooldown);

        if (this.maxUse > 0)
        {
            compoundTag.putInt(ModConstants.Tag.MAX_USE, this.maxUse);
        }

        this.stratagem.unwrapKey().ifPresent(resourceKey -> compoundTag.putString(ModConstants.Tag.STRATAGEM, resourceKey.location().toString()));
        compoundTag.putInt(ModConstants.Tag.ID, this.id);
        compoundTag.putString(ModConstants.Tag.STATE, this.state.getName());
        compoundTag.putString(ModConstants.Tag.SIDE, this.side.getName());
        compoundTag.putBoolean(ModConstants.Tag.SHOULD_DISPLAY, this.shouldDisplay);
    }

    @Nullable
    public static StratagemInstance load(CompoundTag compoundTag, Level level)
    {
        var stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey));

        if (stratagem.isPresent())
        {
            var inboundDuration = 0;
            var duration = -1;
            var maxUse = -1;
            var id = compoundTag.getInt(ModConstants.Tag.ID);
            var state = StratagemState.byName(compoundTag.getString(ModConstants.Tag.STATE));
            var side = Side.byName(compoundTag.getString(ModConstants.Tag.SIDE));
            var shouldDisplay = compoundTag.getBoolean(ModConstants.Tag.SHOULD_DISPLAY);

            if (compoundTag.contains(ModConstants.Tag.INBOUND_DURATION, Tag.TAG_INT))
            {
                inboundDuration = compoundTag.getInt(ModConstants.Tag.INBOUND_DURATION);
            }

            if (compoundTag.contains(ModConstants.Tag.DURATION, Tag.TAG_INT))
            {
                duration = compoundTag.getInt(ModConstants.Tag.DURATION);
            }

            var cooldown = compoundTag.getInt(ModConstants.Tag.COOLDOWN);
            var lastMaxCooldown = compoundTag.getInt(ModConstants.Tag.LAST_MAX_COOLDOWN);

            if (compoundTag.contains(ModConstants.Tag.MAX_USE, Tag.TAG_INT))
            {
                maxUse = compoundTag.getInt(ModConstants.Tag.MAX_USE);
            }
            return new StratagemInstance(id, stratagem.get(), inboundDuration, duration, cooldown, lastMaxCooldown, maxUse, state, side, shouldDisplay);
        }
        return null;
    }

    public void resetStratagemTicks(StratagemProperties properties)
    {
        this.inboundDuration = properties.inboundDuration();
        this.duration = properties.duration();
        this.cooldown = properties.cooldown();
        this.lastMaxCooldown = this.cooldown;
        this.maxUse = properties.maxUse();
    }

    public Holder<Stratagem> getStratagem()
    {
        return this.stratagem;
    }

    public void tick(Player player)
    {
        this.tick(null, player, false);
    }

    public void tick(@Nullable MinecraftServer minecraftServer, @Nullable Player player, boolean isServer)
    {
        this.getRule().tick(StratagemInstanceContext.create(this, minecraftServer, player, isServer));
    }

    public void use(@Nullable MinecraftServer minecraftServer, @Nullable Player player, boolean isServer)
    {
        this.getRule().onUse(StratagemInstanceContext.create(this, minecraftServer, player, isServer));
    }

    public void reset(@Nullable MinecraftServer minecraftServer, @Nullable Player player, boolean isServer)
    {
        this.getRule().onReset(StratagemInstanceContext.create(this, minecraftServer, player, isServer));
    }

    public boolean canUse(Player player)
    {
        return this.canUse(null, player, false);
    }

    public boolean canUse(@Nullable MinecraftServer minecraftServer, @Nullable Player player, boolean isServer)
    {
        return this.getRule().canUse(StratagemInstanceContext.create(this, minecraftServer, player, isServer));
    }

    public String getCode()
    {
        return this.stratagem().code();
    }

    public StratagemRule getRule()
    {
        return this.stratagem().rule();
    }

    public Stratagem stratagem()
    {
        return this.stratagem.value();
    }

    public ResourceKey<Stratagem> getResourceKey()
    {
        return this.stratagem.unwrapKey().orElseThrow();
    }

    public boolean isReady()
    {
        return this.state == StratagemState.READY;
    }

    @Override
    public int compareTo(StratagemInstance instance)
    {
        var builder = new CompareToBuilder();
        return builder.append(this.id, instance.id).build();
    }

    public enum Side
    {
        PLAYER,
        SERVER;

        private static final Side[] VALUES = values();
        public static final IntFunction<Side> BY_ID = ByIdMap.continuous(Side::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, Side> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Side::ordinal);

        public static Side byName(String name)
        {
            for (var state : VALUES)
            {
                if (name.equalsIgnoreCase(state.name()))
                {
                    return state;
                }
            }
            return SERVER;
        }

        public String getName()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}