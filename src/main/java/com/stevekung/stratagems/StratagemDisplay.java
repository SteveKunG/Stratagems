package com.stevekung.stratagems;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public record StratagemDisplay(Type type, Optional<ItemStack> itemStack, Optional<ResourceLocation> texture, Optional<ResolvableProfile> playerIcon, boolean useRemainingAsCount, Optional<String> displayCountOverride)
{
    public static final Codec<StratagemDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(StratagemDisplay::type),
            ItemStack.CODEC.optionalFieldOf("item_stack").forGetter(StratagemDisplay::itemStack),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(StratagemDisplay::texture),
            ResolvableProfile.CODEC.optionalFieldOf("player_icon").forGetter(StratagemDisplay::playerIcon),
            Codec.BOOL.optionalFieldOf("use_remaining_as_count", true).forGetter(StratagemDisplay::useRemainingAsCount),
            Codec.STRING.optionalFieldOf("display_count_override").forGetter(StratagemDisplay::displayCountOverride)
    ).apply(instance, StratagemDisplay::new));

    public enum Type implements StringRepresentable
    {
        ITEM,
        TEXTURE,
        PLAYER_ICON;

        public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);
        private static final Type[] VALUES = values();

        public static Type byName(String name)
        {
            for (var state : VALUES)
            {
                if (name.equalsIgnoreCase(state.name()))
                {
                    return state;
                }
            }
            return ITEM;
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