package com.stevekung.stratagems;

import java.util.Optional;

import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.rule.StratagemRule;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;

public final class StratagemEntry
{
    private final ServerLevel level;
    private final Holder<Stratagem> stratagem;
    public int inboundDuration;
    public Integer duration;
    public int cooldown;
    public Integer remainingUse;
    public StratagemState state;

    public StratagemEntry(ServerLevel level, CompoundTag compoundTag)
    {
        this.level = level;
        this.stratagem = Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString(ModConstants.Tag.STRATAGEM))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).orElseThrow();

        if (compoundTag.contains(ModConstants.Tag.INBOUND_DURATION, Tag.TAG_INT))
        {
            this.inboundDuration = compoundTag.getInt(ModConstants.Tag.INBOUND_DURATION);
        }

        if (compoundTag.contains(ModConstants.Tag.DURATION, Tag.TAG_INT))
        {
            this.duration = compoundTag.getInt(ModConstants.Tag.DURATION);
        }

        this.cooldown = compoundTag.getInt(ModConstants.Tag.COOLDOWN);

        if (compoundTag.contains(ModConstants.Tag.REMAINING_USE, Tag.TAG_INT))
        {
            this.remainingUse = compoundTag.getInt(ModConstants.Tag.REMAINING_USE);
        }

        this.state = StratagemState.byName(compoundTag.getString(ModConstants.Tag.STATE));
    }

    public void tick()
    {
        this.getRule().tick(this);
    }

    public void use(Player player)
    {
        this.getRule().onUse(this, player);
    }

    public void reset()
    {
        this.getRule().onReset(this);
    }

    public void resetStratagemTicks(StratagemProperties properties)
    {
        this.inboundDuration = properties.inboundDuration();

        if (properties.duration().isPresent())
        {
            this.duration = properties.duration().get();
        }

        this.cooldown = properties.cooldown();

        if (properties.remainingUse().isPresent())
        {
            this.remainingUse = properties.remainingUse().get();
        }
    }

    public void save(CompoundTag compoundTag)
    {
        if (this.inboundDuration > 0)
        {
            compoundTag.putInt(ModConstants.Tag.INBOUND_DURATION, this.inboundDuration);
        }

        if (this.duration != null)
        {
            compoundTag.putInt(ModConstants.Tag.DURATION, this.duration);
        }

        compoundTag.putInt(ModConstants.Tag.COOLDOWN, this.cooldown);

        if (this.remainingUse != null)
        {
            compoundTag.putInt(ModConstants.Tag.REMAINING_USE, this.remainingUse);
        }

        this.stratagem.unwrapKey().ifPresent(resourceKey -> compoundTag.putString(ModConstants.Tag.STRATAGEM, resourceKey.location().toString()));
        compoundTag.putString(ModConstants.Tag.STATE, this.state.getName());
    }

    public boolean canUse()
    {
        return this.getRule().canUse(this);
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

    public ServerLevel level()
    {
        return this.level;
    }

    public String formatTickDuration(int duration)
    {
        return Component.translatable("stratagem.menu.tminus").getString() + StringUtil.formatTickDuration(duration, this.level().tickRateManager().tickrate());
    }

    public boolean isReady()
    {
        return this.state == StratagemState.READY;
    }

    public Holder<Stratagem> getStratagem()
    {
        return this.stratagem;
    }
}