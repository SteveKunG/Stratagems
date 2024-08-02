package com.stevekung.stratagems.packet;

import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.StratagemInstance;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnStratagemPacket(ResourceLocation stratagem, StratagemInstance.Side side) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SpawnStratagemPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.SPAWN_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, SpawnStratagemPacket> CODEC = CustomPacketPayload.codec(SpawnStratagemPacket::write, SpawnStratagemPacket::new);

    private SpawnStratagemPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readResourceLocation(), buffer.readEnum(StratagemInstance.Side.class));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeResourceLocation(this.stratagem);
        buffer.writeEnum(this.side);
    }

    @Override
    public CustomPacketPayload.Type<SpawnStratagemPacket> type()
    {
        return TYPE;
    }
}