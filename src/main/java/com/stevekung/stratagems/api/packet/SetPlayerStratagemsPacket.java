package com.stevekung.stratagems.api.packet;

import java.util.Collection;
import java.util.UUID;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.StratagemsData;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetPlayerStratagemsPacket(Collection<StratagemEntryData> playerEntries, UUID uuid) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SetPlayerStratagemsPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.SET_PLAYER_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, SetPlayerStratagemsPacket> CODEC = CustomPacketPayload.codec(SetPlayerStratagemsPacket::write, SetPlayerStratagemsPacket::new);

    private SetPlayerStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(StratagemEntryData::new), buffer.readUUID());
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.playerEntries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
        buffer.writeUUID(this.uuid);
    }

    @Override
    public CustomPacketPayload.Type<SetPlayerStratagemsPacket> type()
    {
        return TYPE;
    }

    public static SetPlayerStratagemsPacket create(StratagemsData stratagemsData, UUID uuid)
    {
        return new SetPlayerStratagemsPacket(StratagemUtils.mapToEntry(stratagemsData), uuid);
    }
}