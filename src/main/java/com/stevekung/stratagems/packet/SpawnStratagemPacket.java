package com.stevekung.stratagems.packet;

import com.stevekung.stratagems.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnStratagemPacket(ResourceLocation stratagem) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SpawnStratagemPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.SPAWN_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, SpawnStratagemPacket> CODEC = CustomPacketPayload.codec(SpawnStratagemPacket::write, SpawnStratagemPacket::new);

    private SpawnStratagemPacket(FriendlyByteBuf buf)
    {
        this(buf.readResourceLocation());
    }

    private void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.stratagem);
    }

    @Override
    public CustomPacketPayload.Type<SpawnStratagemPacket> type()
    {
        return TYPE;
    }
}