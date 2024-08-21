package com.stevekung.stratagems.api;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum StratagemState implements StringRepresentable
{
    READY,
    IN_USE,
    INBOUND,
    COOLDOWN,
    IMPACT,
    UNAVAILABLE,
    BLOCKED;

    public static final Codec<StratagemState> CODEC = StringRepresentable.fromValues(StratagemState::values);
    private static final StratagemState[] VALUES = values();

    public static StratagemState byName(String name)
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

    public Component getTranslationName()
    {
        return Component.translatable("stratagem.state." + this.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String getSerializedName()
    {
        return this.getName();
    }
}