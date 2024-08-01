package com.stevekung.stratagems.packet;

import java.util.UUID;

import com.stevekung.stratagems.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UseReplenishStratagemPacket(ResourceLocation stratagem, UUID uuid) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UseReplenishStratagemPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.USE_REPLENISH_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, UseReplenishStratagemPacket> CODEC = CustomPacketPayload.codec(UseReplenishStratagemPacket::write, UseReplenishStratagemPacket::new);

    private UseReplenishStratagemPacket(FriendlyByteBuf buf)
    {
        this(buf.readResourceLocation(), buf.readUUID());
    }

    private void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.stratagem);
        buf.writeUUID(this.uuid);
    }

    @Override
    public CustomPacketPayload.Type<UseReplenishStratagemPacket> type()
    {
        return TYPE;
    }
}