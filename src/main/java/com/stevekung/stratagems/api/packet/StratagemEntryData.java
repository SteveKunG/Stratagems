package com.stevekung.stratagems.api.packet;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemModifier;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;

public record StratagemEntryData(ResourceKey<Stratagem> stratagem, int id, int inboundDuration, int duration, int cooldown, int lastMaxCooldown, int maxUse, StratagemState state, StratagemInstance.Side side, boolean shouldDisplay, StratagemModifier modifier)
{
    public StratagemEntryData(FriendlyByteBuf buffer)
    {
        this(buffer.readResourceKey(ModRegistries.STRATAGEM), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readEnum(StratagemState.class), buffer.readEnum(StratagemInstance.Side.class), buffer.readBoolean(), buffer.readEnum(StratagemModifier.class));
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeResourceKey(this.stratagem);
        buffer.writeInt(this.id);
        buffer.writeInt(this.inboundDuration);
        buffer.writeInt(this.duration);
        buffer.writeInt(this.cooldown);
        buffer.writeInt(this.lastMaxCooldown);
        buffer.writeInt(this.maxUse);
        buffer.writeEnum(this.state);
        buffer.writeEnum(this.side);
        buffer.writeBoolean(this.shouldDisplay);
        buffer.writeEnum(this.modifier);
    }

    public static StratagemEntryData fromInstance(StratagemInstance instance)
    {
        return new StratagemEntryData(instance.getResourceKey(), instance.id, instance.inboundDuration, instance.duration, instance.cooldown, instance.lastMaxCooldown, instance.maxUse, instance.state, instance.side, instance.shouldDisplay, instance.modifier);
    }
}