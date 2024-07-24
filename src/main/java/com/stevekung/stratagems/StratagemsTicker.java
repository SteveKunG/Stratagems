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
import net.minecraft.util.StringUtil;

public class StratagemsTicker
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ServerLevel level;
    private final Holder<Stratagem> stratagem;
    private int incomingDuration;
    private int duration;
    private int nextUseCooldown;
    private int remainingUse = -1;
    private State state;

    public StratagemsTicker(ServerLevel level, CompoundTag compoundTag)
    {
        this.level = level;
        this.stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).orElseThrow();
        this.setDefaultStratagemTicks();
        this.stratagem.value().properties().remainingUse().ifPresent(remainingUse -> this.remainingUse = remainingUse);
        this.state = State.byName(compoundTag.getString(ModConstants.Tag.STATE));
    }

    public void tick()
    {
        if (!this.isReady())
        {
            if (this.state == State.IN_USE)
            {
                if (this.duration > 0)
                {
                    this.duration--;

                    if (this.duration % 20 == 0)
                    {
                        LOGGER.info("{}, duration:{}", this.stratagem.getRegisteredName(), StringUtil.formatTickDuration(this.duration, this.level.tickRateManager().tickrate()));
                    }
                }
                else
                {
                    this.state = State.INCOMING;
                    LOGGER.info("{}, switch to state:{}", this.stratagem.getRegisteredName(), this.state);
                }
            }

            if (this.state == State.INCOMING && this.incomingDuration > 0)
            {
                this.incomingDuration--;

                if (this.incomingDuration % 20 == 0)
                {
                    LOGGER.info("{}, incomingDuration:{}", this.stratagem.getRegisteredName(), StringUtil.formatTickDuration(this.incomingDuration, this.level.tickRateManager().tickrate()));
                }
            }

            if (this.state != State.COOLDOWN && this.incomingDuration == 0)
            {
                this.state = State.COOLDOWN;
                this.nextUseCooldown = this.stratagem.value().properties().nextUseCooldown();
                LOGGER.info("{}, switch to state:{}", this.stratagem.getRegisteredName(), this.state);
            }

            if (this.state == State.COOLDOWN)
            {
                if (this.nextUseCooldown > 0)
                {
                    this.nextUseCooldown--;

                    if (this.nextUseCooldown % 20 == 0)
                    {
                        LOGGER.info("{}, state:{}, nextUseCooldown:{}", this.stratagem.getRegisteredName(), this.state, StringUtil.formatTickDuration(this.nextUseCooldown, this.level.tickRateManager().tickrate()));
                    }
                }

                if (this.nextUseCooldown == 0)
                {
                    this.state = State.READY;
                    this.setDefaultStratagemTicks();
                    LOGGER.info("{}, switch to state:{}", this.stratagem.getRegisteredName(), this.state);
                }
            }
        }
    }

    public void useStratagem()
    {
        if (this.remainingUse == 0)
        {
            LOGGER.info("Cannot use this stratagem! {}", this.stratagem);
            return;
        }

        this.state = State.IN_USE;

        if (this.remainingUse > 0)
        {
            this.remainingUse--;
            LOGGER.info("{}, remainingUse:{}", this.stratagem.getRegisteredName(), this.remainingUse);
        }
    }

    private void setDefaultStratagemTicks()
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

    public boolean isReady()
    {
        return this.state == State.READY;
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