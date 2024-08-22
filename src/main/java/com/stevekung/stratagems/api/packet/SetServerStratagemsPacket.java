package com.stevekung.stratagems.api.packet;

import java.util.Collection;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.StratagemsData;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetServerStratagemsPacket(Collection<StratagemEntryData> serverEntries) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SetServerStratagemsPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.SET_SERVER_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, SetServerStratagemsPacket> CODEC = CustomPacketPayload.codec(SetServerStratagemsPacket::write, SetServerStratagemsPacket::new);

    private SetServerStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(StratagemEntryData::new));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.serverEntries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
    }

    @Override
    public CustomPacketPayload.Type<SetServerStratagemsPacket> type()
    {
        return TYPE;
    }

    public static SetServerStratagemsPacket create(StratagemsData stratagemsData)
    {
        return new SetServerStratagemsPacket(StratagemUtils.mapToEntry(stratagemsData));
    }
}