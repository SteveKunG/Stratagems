package com.stevekung.stratagems.api.packet;

import java.util.Locale;
import java.util.UUID;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.ModConstants;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;

public record UpdateStratagemPacket(Action action, StratagemEntryData entryData, @Nullable UUID uuid) implements CustomPacketPayload
{
    public static final Type<UpdateStratagemPacket> TYPE = new Type<>(ModConstants.Packets.UPDATE_STRATAGEM);
    public static final StreamCodec<FriendlyByteBuf, UpdateStratagemPacket> CODEC = CustomPacketPayload.codec(UpdateStratagemPacket::write, UpdateStratagemPacket::new);

    public UpdateStratagemPacket(Action action, StratagemEntryData entryData)
    {
        this(action, entryData, null);
    }

    private UpdateStratagemPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readEnum(Action.class), new StratagemEntryData(buffer), buffer.readNullable(bufferx -> bufferx.readUUID()));
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.action);
        this.entryData.write(buffer);
        buffer.writeNullable(this.uuid, (bufferx, uuid) -> bufferx.writeUUID(uuid));
    }

    @Override
    public Type<UpdateStratagemPacket> type()
    {
        return TYPE;
    }

    public enum Action
    {
        UPDATE,
        ADD,
        REMOVE;

        private static final Action[] VALUES = values();
        public static final IntFunction<Action> BY_ID = ByIdMap.continuous(Action::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, Action> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Action::ordinal);

        public static Action byName(String name)
        {
            for (var state : VALUES)
            {
                if (name.equalsIgnoreCase(state.name()))
                {
                    return state;
                }
            }
            return UPDATE;
        }

        public String getName()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}