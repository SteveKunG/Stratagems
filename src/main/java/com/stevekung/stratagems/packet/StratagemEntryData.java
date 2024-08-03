package com.stevekung.stratagems.packet;

import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.ModRegistries;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;

public record StratagemEntryData(ResourceKey<Stratagem> stratagem, int inboundDuration, Integer duration, int cooldown, Integer remainingUse, StratagemState state, StratagemInstance.Side side)
{
    public StratagemEntryData(FriendlyByteBuf buffer)
    {
        this(buffer.readResourceKey(ModRegistries.STRATAGEM), buffer.readInt(), buffer.readNullable(FriendlyByteBuf::readInt), buffer.readInt(), buffer.readNullable(FriendlyByteBuf::readInt), buffer.readEnum(StratagemState.class), buffer.readEnum(StratagemInstance.Side.class));
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeResourceKey(this.stratagem);
        buffer.writeInt(this.inboundDuration);
        buffer.writeNullable(this.duration, FriendlyByteBuf::writeInt);
        buffer.writeInt(this.cooldown);
        buffer.writeNullable(this.remainingUse, FriendlyByteBuf::writeInt);
        buffer.writeEnum(this.state);
        buffer.writeEnum(this.side);
    }
}