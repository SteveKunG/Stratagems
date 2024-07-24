package com.stevekung.stratagems;

import java.util.Locale;
import java.util.Optional;

import com.stevekung.stratagems.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class StratagemsTicker
{
    private final ServerLevel level;
    private final Holder<Stratagem> stratagem;
    private int incomingDuration;
    private int duration;
    private int nextUseCooldown;
    private State state;

    public StratagemsTicker(ServerLevel level, CompoundTag compoundTag)
    {
        this.level = level;
        this.stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).orElseThrow();
        this.resetStratagemTick();
        this.state = State.READY;
    }

    public void tick()
    {
        if (this.state != State.READY)
        {
            if (this.state == State.IN_USE && this.duration > 0)
            {
                this.duration--;
            }

            if (this.state == State.INCOMING && this.incomingDuration > 0)
            {
                this.incomingDuration--;
            }

            if (this.duration == 0 || this.incomingDuration == 0)
            {
                this.state = State.COOLDOWN;
            }

            if (this.state == State.COOLDOWN)
            {
                if (this.nextUseCooldown > 0)
                {
                    this.nextUseCooldown--;
                }

                if (this.incomingDuration == 0 && this.nextUseCooldown == 0)
                {
                    this.state = State.READY;
                }
            }
        }
        else
        {
            this.resetStratagemTick();
        }
    }

    public void useStratagem()
    {
        this.state = State.IN_USE;
    }

    private void resetStratagemTick()
    {
        var properties = this.stratagem.value().properties();
        this.incomingDuration = properties.incomingDuration();
        this.duration = properties.duration().orElse(0);
        this.nextUseCooldown = properties.nextUseCooldown();
    }

    public void save(CompoundTag compoundTag)
    {
        compoundTag.putInt(ModConstants.Tag.INCOMING_DURATION, this.incomingDuration);
        compoundTag.putInt(ModConstants.Tag.DURATION, this.duration);
        compoundTag.putInt(ModConstants.Tag.NEXT_USE_COOLDOWN, this.nextUseCooldown);
        this.stratagem.unwrapKey().ifPresent(resourceKey -> compoundTag.putString(ModConstants.Tag.STRATAGEM, resourceKey.location().toString()));
        compoundTag.putString(ModConstants.Tag.STATE, this.state.getName());
    }

    public Holder<Stratagem> getStratagem()
    {
        return this.stratagem;
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