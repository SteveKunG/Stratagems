package com.stevekung.stratagems.api.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.stevekung.stratagems.api.ModConstants;

public record ClearInputPacket() implements CustomPacketPayload
{
    public static final Type<ClearInputPacket> TYPE = new Type<>(ModConstants.Packets.CLEAR_INPUT);
    public static final StreamCodec<FriendlyByteBuf, ClearInputPacket> CODEC = CustomPacketPayload.codec(ClearInputPacket::write, ClearInputPacket::new);

    private ClearInputPacket(FriendlyByteBuf buffer)
    {
        this();
    }

    private void write(FriendlyByteBuf buffer)
    {
    }

    @Override
    public Type<ClearInputPacket> type()
    {
        return TYPE;
    }
}