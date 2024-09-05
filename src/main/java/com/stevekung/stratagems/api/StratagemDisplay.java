package com.stevekung.stratagems.api;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public record StratagemDisplay(Type type, Optional<ItemStack> itemStack, Optional<ResourceLocation> texture, Optional<ResolvableProfile> playerIcon, boolean maxUseAsCount, Optional<String> displayCountOverride)
{
    public static final Codec<StratagemDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(StratagemDisplay::type),
            ItemStack.CODEC.optionalFieldOf("item").forGetter(StratagemDisplay::itemStack),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(StratagemDisplay::texture),
            ResolvableProfile.CODEC.optionalFieldOf("player_icon").forGetter(StratagemDisplay::playerIcon),
            Codec.BOOL.optionalFieldOf("max_use_as_count", true).forGetter(StratagemDisplay::maxUseAsCount),
            Codec.STRING.optionalFieldOf("display_count_override").forGetter(StratagemDisplay::displayCountOverride)
    ).apply(instance, StratagemDisplay::new));

    public enum Type implements StringRepresentable
    {
        ITEM,
        TEXTURE,
        PLAYER_ICON;

        public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);

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