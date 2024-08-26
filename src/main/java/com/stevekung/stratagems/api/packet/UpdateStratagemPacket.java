package com.stevekung.stratagems.api.packet;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateStratagemPacket(Action action, StratagemEntryData entryData, @Nullable UUID uuid) implements CustomPacketPayload
{
    public static final Type<UpdateStratagemPacket> TYPE = new Type<>(ModConstants.Packets.UPDATE_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, UpdateStratagemPacket> CODEC = CustomPacketPayload.codec(UpdateStratagemPacket::write, UpdateStratagemPacket::new);

    public UpdateStratagemPacket(Action action, StratagemEntryData entryData)
    {
        this(action, entryData, null);
    }

    private UpdateStratagemPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readEnum(Action.class), new StratagemEntryData(buffer), buffer.readNullable(bufferx -> bufferx.readUUID()));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.action);
        this.entryData.write(buffer);
        buffer.writeNullable(this.uuid, (bufferx, uuid) -> bufferx.writeUUID(uuid));
    }

    @Override
    public Type<UpdateStratagemPacket> type()
    {
        return TYPE;
    }

    public enum Action
    {
        UPDATE,
        ADD,
        REMOVE
    }
}