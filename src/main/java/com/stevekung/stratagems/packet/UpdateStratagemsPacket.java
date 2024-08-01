package com.stevekung.stratagems.packet;

import java.util.List;

import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.ModRegistries;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record UpdateStratagemsPacket(List<StratagemEntryData> entries) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UpdateStratagemsPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.SPAWN_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, UpdateStratagemsPacket> CODEC = CustomPacketPayload.codec(UpdateStratagemsPacket::write, UpdateStratagemsPacket::new);

    private UpdateStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(StratagemEntryData::new));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.entries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
    }

    @Override
    public CustomPacketPayload.Type<UpdateStratagemsPacket> type()
    {
        return TYPE;
    }

    public static record StratagemEntryData(ResourceKey<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int remainingUse, StratagemState state)
    {
        public StratagemEntryData(FriendlyByteBuf buffer)
        {
            this(buffer.readResourceKey(ModRegistries.STRATAGEM), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readEnum(StratagemState.class));
        }

        public void write(FriendlyByteBuf buffer)
        {
            buffer.writeResourceKey(this.stratagem);
            buffer.writeInt(this.inboundDuration);
            buffer.writeInt(this.duration);
            buffer.writeInt(this.cooldown);
            buffer.writeInt(this.remainingUse);
            buffer.writeEnum(this.state);
        }
    }
}