package com.stevekung.stratagems.packet;

import java.util.List;
import java.util.UUID;

import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.StratagemInstance;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdatePlayerStratagemsPacket(List<UpdateServerStratagemsPacket.StratagemEntryData> entries, UUID uuid) implements CustomPacketPayload
{
    public static final Type<UpdatePlayerStratagemsPacket> TYPE = new Type<>(ModConstants.Packets.UPDATE_PLAYER_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, UpdatePlayerStratagemsPacket> CODEC = CustomPacketPayload.codec(UpdatePlayerStratagemsPacket::write, UpdatePlayerStratagemsPacket::new);

    private UpdatePlayerStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(UpdateServerStratagemsPacket.StratagemEntryData::new), buffer.readUUID());
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.entries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
        buffer.writeUUID(this.uuid);
    }

    @Override
    public Type<UpdatePlayerStratagemsPacket> type()
    {
        return TYPE;
    }

    public static UpdatePlayerStratagemsPacket mapInstanceToEntry(List<StratagemInstance> instances, UUID uuid)
    {
        return new UpdatePlayerStratagemsPacket(instances.stream().map(instance -> new UpdateServerStratagemsPacket.StratagemEntryData(instance.getStratagem().unwrapKey().orElseThrow(), instance.inboundDuration, instance.duration, instance.cooldown, instance.remainingUse, instance.state, instance.side)).toList(), uuid);
    }
}