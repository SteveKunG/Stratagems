package com.stevekung.stratagems.packet;

import java.util.List;

import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.ModRegistries;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record UpdateServerStratagemsPacket(List<StratagemEntryData> entries) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UpdateServerStratagemsPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.UPDATE_SERVER_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, UpdateServerStratagemsPacket> CODEC = CustomPacketPayload.codec(UpdateServerStratagemsPacket::write, UpdateServerStratagemsPacket::new);

    private UpdateServerStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(StratagemEntryData::new));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.entries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
    }

    @Override
    public CustomPacketPayload.Type<UpdateServerStratagemsPacket> type()
    {
        return TYPE;
    }

    public static UpdateServerStratagemsPacket mapInstanceToEntry(List<StratagemInstance> instances)
    {
        return new UpdateServerStratagemsPacket(instances.stream().map(instance -> new StratagemEntryData(instance.getStratagem().value().id(), instance.inboundDuration, instance.duration, instance.cooldown, instance.remainingUse, instance.state)).toList());
    }

    public static List<StratagemInstance> mapEntryToInstance(List<StratagemEntryData> entries, RegistryAccess registryAccess)
    {
        return entries.stream().map(entry -> new StratagemInstance(registryAccess.lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.remainingUse(), entry.state())).toList();
    }

    public record StratagemEntryData(ResourceKey<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int remainingUse, StratagemState state)
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