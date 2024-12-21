package com.stevekung.stratagems.api;

import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum StratagemModifier implements StringRepresentable
{
    NONE(0, "none"),
    RANDOMIZE(1, "randomize");

    @SuppressWarnings("deprecation")
    public static final StringRepresentable.EnumCodec<StratagemModifier> CODEC = StringRepresentable.fromEnum(StratagemModifier::values);
    public static final IntFunction<StratagemModifier> BY_ID = ByIdMap.continuous(StratagemModifier::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private static final StratagemModifier[] VALUES = values();
    private final int id;
    private final String name;

    StratagemModifier(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @Nullable
    public static StratagemModifier byName(String name)
    {
        for (var state : VALUES)
        {
            if (name.equalsIgnoreCase(state.name()))
            {
                return state;
            }
        }
        return NONE;
    }

    public int id()
    {
        return this.id;
    }

    public Component getTranslatedName()
    {
        return Component.translatable("stratagem.modifier." + this.getSerializedName());
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }
}