package com.stevekung.stratagems;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.rule.StratagemRule;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StratagemInstance
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<StratagemInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Stratagem.CODEC.fieldOf(ModConstants.Tag.STRATAGEM).forGetter(StratagemInstance::getStratagem),
            Codec.INT.fieldOf(ModConstants.Tag.INBOUND_DURATION).forGetter(StratagemInstance::getInboundDuration),
            Codec.INT.fieldOf(ModConstants.Tag.DURATION).forGetter(StratagemInstance::getDuration),
            Codec.INT.fieldOf(ModConstants.Tag.COOLDOWN).forGetter(StratagemInstance::getCooldown),
            Codec.INT.fieldOf(ModConstants.Tag.REMAINING_USE).forGetter(StratagemInstance::getRemainingUse),
            StratagemState.CODEC.fieldOf(ModConstants.Tag.STATE).forGetter(StratagemInstance::getState)
    ).apply(instance, StratagemInstance::new));

    private final Holder<Stratagem> stratagem;
    private final ResourceKey<Stratagem> stratagemResourceKey;
    public int inboundDuration;
    public Integer duration;
    public int cooldown;
    public Integer remainingUse;
    public StratagemState state;

    public StratagemInstance(Holder<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int remainingUse, StratagemState state)
    {
        this.stratagem = stratagem;
        this.stratagemResourceKey = stratagem.value().id();
        this.inboundDuration = inboundDuration;
        this.duration = duration;
        this.cooldown = cooldown;
        this.remainingUse = remainingUse;
        this.state = state;
    }

    public Tag save()
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    @Nullable
    public static StratagemInstance load(CompoundTag nbt)
    {
        return CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(LOGGER::error).orElse(null);
    }

    public void resetStratagemTicks(StratagemProperties properties)
    {
        this.inboundDuration = properties.inboundDuration();
        this.duration = properties.duration().orElse(-1);
        this.cooldown = properties.cooldown();
        this.remainingUse = properties.remainingUse().orElse(-1);
    }

    public Holder<Stratagem> getStratagem()
    {
        return this.stratagem;
    }

    public int getInboundDuration()
    {
        return this.inboundDuration;
    }

    public Integer getDuration()
    {
        return this.duration;
    }

    public int getCooldown()
    {
        return this.cooldown;
    }

    public Integer getRemainingUse()
    {
        return this.remainingUse;
    }

    public StratagemState getState()
    {
        return this.state;
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
        return this.stratagemResourceKey;
    }

    public String formatTickDuration(int duration, Player player)
    {
        if (player == null)
        {
            return Component.translatable("stratagem.menu.tminus").getString() + StringUtil.formatTickDuration(duration, 20.0F);
        }
        return this.formatTickDuration(duration, player.level());
    }

    public String formatTickDuration(int duration, Level level)
    {
        return Component.translatable("stratagem.menu.tminus").getString() + StringUtil.formatTickDuration(duration, level.tickRateManager().tickrate());
    }

    public boolean isReady()
    {
        return this.state == StratagemState.READY;
    }
}