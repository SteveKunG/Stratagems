package com.stevekung.stratagems;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class StratagemsTicker
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public final ServerLevel level;
    private final Holder<Stratagem> stratagem;
    public int incomingDuration;
    public int duration;
    public int nextUseCooldown;
    public int remainingUse = -1;
    public State state;

    public StratagemsTicker(ServerLevel level, CompoundTag compoundTag)
    {
        this.level = level;
        this.stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).orElseThrow();
        this.incomingDuration = compoundTag.getInt(ModConstants.Tag.INCOMING_DURATION);
        this.duration = compoundTag.getInt(ModConstants.Tag.DURATION);
        this.nextUseCooldown = compoundTag.getInt(ModConstants.Tag.NEXT_USE_COOLDOWN);
        this.remainingUse = compoundTag.getInt(ModConstants.Tag.REMAINING_USE);
        this.state = State.byName(compoundTag.getString(ModConstants.Tag.STATE));
    }

    public void tick()
    {
        this.stratagem().rule().tick(this);
    }

    public void useStratagem()
    {
        this.stratagem().rule().onUse(this);
    }

    public void setDefaultStratagemTicks(StratagemProperties properties)
    {
        this.incomingDuration = properties.incomingDuration();
        this.duration = properties.duration().orElse(0);
        this.nextUseCooldown = properties.nextUseCooldown();
    }

    public void save(CompoundTag compoundTag)
    {
        compoundTag.putInt(ModConstants.Tag.INCOMING_DURATION, this.incomingDuration);
        compoundTag.putInt(ModConstants.Tag.DURATION, this.duration);
        compoundTag.putInt(ModConstants.Tag.NEXT_USE_COOLDOWN, this.nextUseCooldown);
        compoundTag.putInt(ModConstants.Tag.REMAINING_USE, this.remainingUse);
        this.stratagem.unwrapKey().ifPresent(resourceKey -> compoundTag.putString(ModConstants.Tag.STRATAGEM, resourceKey.location().toString()));
        compoundTag.putString(ModConstants.Tag.STATE, this.state.getName());
    }

    public boolean canUse()
    {
        return this.stratagem().rule().canUse(this);
    }

    public boolean isReady()
    {
        return this.state == State.READY;
    }

    public Holder<Stratagem> getStratagem()
    {
        return this.stratagem;
    }

    public Stratagem stratagem()
    {
        return this.stratagem.value();
    }

    public enum State
    {
        READY,
        IN_USE,
        INCOMING,
        COOLDOWN,
        BLOCKED;

        private static final State[] VALUES = values();

        static State byName(String name)
        {
            for (var state : VALUES)
            {
                if (name.equalsIgnoreCase(state.name()))
                {
                    return state;
                }
            }
            return READY;
        }

        public String getName()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}