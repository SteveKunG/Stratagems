package com.stevekung.stratagems.packet;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
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

public record UpdateStratagemsPacket(List<StratagemEntryData> serverEntries, List<StratagemEntryData> playerEntries, UUID uuid) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UpdateStratagemsPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.UPDATE_STRATAGEMS);
    public static final StreamCodec<FriendlyByteBuf, UpdateStratagemsPacket> CODEC = CustomPacketPayload.codec(UpdateStratagemsPacket::write, UpdateStratagemsPacket::new);

    private UpdateStratagemsPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readList(StratagemEntryData::new), buffer.readList(StratagemEntryData::new), buffer.readUUID());
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.serverEntries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
        buffer.writeCollection(this.playerEntries, (friendlyByteBuf, entryData) -> entryData.write(friendlyByteBuf));
        buffer.writeUUID(this.uuid);
    }

    @Override
    public CustomPacketPayload.Type<UpdateStratagemsPacket> type()
    {
        return TYPE;
    }

    public static UpdateStratagemsPacket create(List<StratagemInstance> serverInstances, List<StratagemInstance> playerInstances, UUID uuid)
    {
        return new UpdateStratagemsPacket(mapToEntry(serverInstances), mapToEntry(playerInstances), uuid);
    }

    private static List<StratagemEntryData> mapToEntry(List<StratagemInstance> list)
    {
        return list.stream().map(instance -> new StratagemEntryData(instance.getStratagem().unwrapKey().orElseThrow(), instance.inboundDuration, instance.duration, instance.cooldown, instance.remainingUse, instance.state, instance.side)).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
    }

    public static List<StratagemInstance> mapEntryToInstance(List<StratagemEntryData> entries, RegistryAccess registryAccess)
    {
        return entries.stream().map(entry -> new StratagemInstance(registryAccess.lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(entry.stratagem()), entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.remainingUse(), entry.state(), entry.side())).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
    }

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
}