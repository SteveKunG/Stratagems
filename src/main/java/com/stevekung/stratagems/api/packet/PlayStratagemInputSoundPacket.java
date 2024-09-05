package com.stevekung.stratagems.api.packet;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayStratagemInputSoundPacket(SoundType soundType, int inputLength) implements CustomPacketPayload
{
    public static final Type<PlayStratagemInputSoundPacket> TYPE = new Type<>(ModConstants.Packets.STRATAGEM_INPUT_SOUND);
    public static final StreamCodec<FriendlyByteBuf, PlayStratagemInputSoundPacket> CODEC = CustomPacketPayload.codec(PlayStratagemInputSoundPacket::write, PlayStratagemInputSoundPacket::new);

    public PlayStratagemInputSoundPacket(SoundType soundType)
    {
        this(soundType, -1);
    }

    private PlayStratagemInputSoundPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readEnum(SoundType.class), buffer.readInt());
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.soundType);
        buffer.writeInt(this.inputLength);
    }

    @Override
    public Type<PlayStratagemInputSoundPacket> type()
    {
        return TYPE;
    }

    public enum SoundType
    {
        CLICK,
        FAIL,
        SELECT;
    }
}