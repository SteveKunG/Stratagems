package com.stevekung.stratagems.api.packet;

import java.util.UUID;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record UseReplenishStratagemPacket(ResourceKey<Stratagem> stratagem, StratagemInstance.Side side, UUID uuid) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UseReplenishStratagemPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.USE_REPLENISH_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, UseReplenishStratagemPacket> CODEC = CustomPacketPayload.codec(UseReplenishStratagemPacket::write, UseReplenishStratagemPacket::new);

    private UseReplenishStratagemPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readResourceKey(ModRegistries.STRATAGEM), buffer.readEnum(StratagemInstance.Side.class), buffer.readUUID());
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeResourceKey(this.stratagem);
        buffer.writeEnum(this.side);
        buffer.writeUUID(this.uuid);
    }

    @Override
    public CustomPacketPayload.Type<UseReplenishStratagemPacket> type()
    {
        return TYPE;
    }
}