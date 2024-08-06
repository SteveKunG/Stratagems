package com.stevekung.stratagems.api.packet;

import java.util.Collection;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateServerStratagemsPacket(Collection<StratagemEntryData> serverEntries) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UpdateServerStratagemsPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.UPDATE_SERVER_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, UpdateServerStratagemsPacket> CODEC = CustomPacketPayload.codec(UpdateServerStratagemsPacket::write, UpdateServerStratagemsPacket::new);

    private UpdateServerStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(StratagemEntryData::new));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.serverEntries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
    }

    @Override
    public CustomPacketPayload.Type<UpdateServerStratagemsPacket> type()
    {
        return TYPE;
    }

    public static UpdateServerStratagemsPacket create(Collection<StratagemInstance> serverInstances)
    {
        return new UpdateServerStratagemsPacket(StratagemUtils.mapToEntry(serverInstances));
    }
}