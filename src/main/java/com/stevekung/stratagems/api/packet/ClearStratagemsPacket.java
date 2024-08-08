package com.stevekung.stratagems.api.packet;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClearStratagemsPacket(boolean server, boolean player, @Nullable UUID uuid) implements CustomPacketPayload
{
    public static final Type<ClearStratagemsPacket> TYPE = new Type<>(ModConstants.Packets.CLEAR_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, ClearStratagemsPacket> CODEC = CustomPacketPayload.codec(ClearStratagemsPacket::write, ClearStratagemsPacket::new);

    public ClearStratagemsPacket(boolean server)
    {
        this(server, false, null);
    }

    private ClearStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readBoolean(), buffer.readBoolean(), buffer.readNullable(bufferx -> bufferx.readUUID()));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.server);
        buffer.writeBoolean(this.player);
        buffer.writeNullable(this.uuid, (bufferx, uuid) -> bufferx.writeUUID(uuid));
    }

    @Override
    public Type<ClearStratagemsPacket> type()
    {
        return TYPE;
    }
}