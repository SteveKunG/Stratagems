package com.stevekung.stratagems.api;

import java.util.Locale;
import java.util.Optional;
import java.util.function.IntFunction;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.rule.StratagemRule;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StratagemInstance implements Comparable<StratagemInstance>
{
    public static final MapCodec<StratagemInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.fieldOf("id").forGetter(stratagem -> stratagem.id),
                            RegistryFixedCodec.create(ModRegistries.STRATAGEM).fieldOf("stratagem").forGetter(stratagem -> stratagem.stratagem),
                            Codec.INT.fieldOf("inbound_duration").forGetter(stratagem -> stratagem.inboundDuration),
                            Codec.INT.fieldOf("duration").forGetter(stratagem -> stratagem.duration),
                            Codec.INT.fieldOf("cooldown").forGetter(stratagem -> stratagem.cooldown),
                            Codec.INT.fieldOf("last_max_cooldown").forGetter(stratagem -> stratagem.lastMaxCooldown),
                            Codec.INT.fieldOf("max_use").forGetter(stratagem -> stratagem.maxUse),
                            StratagemState.CODEC.fieldOf("state").forGetter(stratagem -> stratagem.state),
                            Side.CODEC.fieldOf("side").forGetter(stratagem -> stratagem.side),
                            Codec.BOOL.fieldOf("should_display").forGetter(stratagem -> stratagem.shouldDisplay),
                            StratagemModifier.CODEC.fieldOf("modifier").forGetter(stratagem -> stratagem.modifier)
                    )
                    .apply(instance, StratagemInstance::new));

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
    public StratagemModifier modifier;

    public StratagemInstance(int id, Holder<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int lastMaxCooldown, int maxUse, StratagemState state, Side side, boolean shouldDisplay, StratagemModifier modifier)
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
        this.modifier = modifier;
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
        compoundTag.putString(ModConstants.Tag.MODIFIER, this.modifier.getSerializedName());
    }

    @Nullable
    public static StratagemInstance load(CompoundTag compoundTag, Level level)
    {
        var stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM).orElseThrow())).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).get(resourceKey));

        if (stratagem.isPresent())
        {
            var inboundDuration = compoundTag.getIntOr(ModConstants.Tag.INBOUND_DURATION, 0);
            var duration = compoundTag.getIntOr(ModConstants.Tag.DURATION, -1);
            var maxUse = compoundTag.getIntOr(ModConstants.Tag.MAX_USE, -1);
            var id = compoundTag.getInt(ModConstants.Tag.ID).orElseThrow();
            var state = StratagemState.byName(compoundTag.getString(ModConstants.Tag.STATE).orElse(StratagemState.READY.getName()));
            var side = Side.byName(compoundTag.getString(ModConstants.Tag.SIDE).orElse(Side.SERVER.getName()));
            var shouldDisplay = compoundTag.getBooleanOr(ModConstants.Tag.SHOULD_DISPLAY, true);
            var modifier = StratagemModifier.byName(compoundTag.getString(ModConstants.Tag.MODIFIER).orElse(StratagemModifier.NONE.getSerializedName()));
            var cooldown = compoundTag.getIntOr(ModConstants.Tag.COOLDOWN, 0);
            var lastMaxCooldown = compoundTag.getIntOr(ModConstants.Tag.LAST_MAX_COOLDOWN, 0);
            return new StratagemInstance(id, stratagem.get(), inboundDuration, duration, cooldown, lastMaxCooldown, maxUse, state, side, shouldDisplay, modifier);
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

    public void block(@Nullable MinecraftServer minecraftServer, @Nullable Player player, boolean isServer, boolean unblock)
    {
        this.getRule().onBlocked(StratagemInstanceContext.create(this, minecraftServer, player, isServer), unblock);
    }

    public void modified(@Nullable MinecraftServer minecraftServer, @Nullable Player player, boolean isServer, StratagemModifier modifier, boolean clear)
    {
        this.getRule().onModified(StratagemInstanceContext.create(this, minecraftServer, player, isServer), modifier, clear);
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

    public enum Side implements StringRepresentable
    {
        PLAYER,
        SERVER;

        private static final Side[] VALUES = values();
        public static final IntFunction<Side> BY_ID = ByIdMap.continuous(Side::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Side> CODEC = StringRepresentable.fromEnum(Side::values);
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

        @Override
        public String getSerializedName()
        {
            return this.getName();
        }
    }
}