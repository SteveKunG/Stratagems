package com.stevekung.stratagems.api.packet;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;

public record StratagemEntryData(ResourceKey<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int remainingUse, StratagemState state, StratagemInstance.Side side)
{
    public StratagemEntryData(FriendlyByteBuf buffer)
    {
        this(buffer.readResourceKey(ModRegistries.STRATAGEM), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readEnum(StratagemState.class), buffer.readEnum(StratagemInstance.Side.class));
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeResourceKey(this.stratagem);
        buffer.writeInt(this.inboundDuration);
        buffer.writeInt(this.duration);
        buffer.writeInt(this.cooldown);
        buffer.writeInt(this.remainingUse);
        buffer.writeEnum(this.state);
        buffer.writeEnum(this.side);
    }
}