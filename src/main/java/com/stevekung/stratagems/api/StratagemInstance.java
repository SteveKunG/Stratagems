package com.stevekung.stratagems.api;

import java.util.Locale;
import java.util.Optional;
import java.util.function.IntFunction;

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

public class StratagemInstance
{
    private final Holder<Stratagem> stratagem;
    public int inboundDuration;
    public int duration;
    public int cooldown;
    public int remainingUse;
    public StratagemState state;
    public Side side;

    public StratagemInstance(Holder<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int remainingUse, StratagemState state, Side side)
    {
        this.stratagem = stratagem;
        this.inboundDuration = inboundDuration;
        this.duration = duration;
        this.cooldown = cooldown;
        this.remainingUse = remainingUse;
        this.state = state;
        this.side = side;
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

        if (this.remainingUse > 0)
        {
            compoundTag.putInt(ModConstants.Tag.REMAINING_USE, this.remainingUse);
        }

        this.stratagem.unwrapKey().ifPresent(resourceKey -> compoundTag.putString(ModConstants.Tag.STRATAGEM, resourceKey.location().toString()));
        compoundTag.putString(ModConstants.Tag.STATE, this.state.getName());
        compoundTag.putString(ModConstants.Tag.SIDE, this.side.getName());
    }

    public static StratagemInstance load(CompoundTag compoundTag, Level level)
    {
        var stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).orElseThrow();
        var inboundDuration = 0;
        var duration = -1;
        var remainingUse = -1;
        var state = StratagemState.byName(compoundTag.getString(ModConstants.Tag.STATE));
        var side = Side.byName(compoundTag.getString(ModConstants.Tag.SIDE));

        if (compoundTag.contains(ModConstants.Tag.INBOUND_DURATION, Tag.TAG_INT))
        {
            inboundDuration = compoundTag.getInt(ModConstants.Tag.INBOUND_DURATION);
        }

        if (compoundTag.contains(ModConstants.Tag.DURATION, Tag.TAG_INT))
        {
            duration = compoundTag.getInt(ModConstants.Tag.DURATION);
        }

        var cooldown = compoundTag.getInt(ModConstants.Tag.COOLDOWN);

        if (compoundTag.contains(ModConstants.Tag.REMAINING_USE, Tag.TAG_INT))
        {
            remainingUse = compoundTag.getInt(ModConstants.Tag.REMAINING_USE);
        }

        return new StratagemInstance(stratagem, inboundDuration, duration, cooldown, remainingUse, state, side);
    }

    public void resetStratagemTicks(StratagemProperties properties)
    {
        this.inboundDuration = properties.inboundDuration();
        this.duration = properties.duration();
        this.cooldown = properties.cooldown();
        this.remainingUse = properties.remainingUse();
    }

    public Holder<Stratagem> getStratagem()
    {
        return this.stratagem;
    }

    public void tick(@Nullable MinecraftServer minecraftServer, @Nullable Player player)
    {
        this.getRule().tick(StratagemInstanceContext.create(this, minecraftServer, player));
    }

    public void use(@Nullable MinecraftServer minecraftServer, @Nullable Player player)
    {
        this.getRule().onUse(StratagemInstanceContext.create(this, minecraftServer, player));
    }

    public void reset(@Nullable MinecraftServer minecraftServer, @Nullable Player player)
    {
        this.getRule().onReset(StratagemInstanceContext.create(this, minecraftServer, player));
    }

    public boolean canUse(@Nullable MinecraftServer minecraftServer, @Nullable Player player)
    {
        return this.getRule().canUse(StratagemInstanceContext.create(this, minecraftServer, player));
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