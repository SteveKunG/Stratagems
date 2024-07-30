package com.stevekung.stratagems.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.action.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;

public class StratagemActions
{
    private static final Codec<StratagemAction> TYPED_CODEC = ModBuiltInRegistries.STRATAGEM_ACTION_TYPE.byNameCodec().dispatch("type", StratagemAction::getType, StratagemActionType::codec);
    public static final Codec<StratagemAction> DIRECT_CODEC = Codec.lazyInitialized(() -> TYPED_CODEC);
    public static final Codec<Holder<StratagemAction>> CODEC = RegistryFileCodec.create(ModRegistries.STRATAGEM_ACTION, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<StratagemAction>> STREAM_CODEC = ByteBufCodecs.holderRegistry(ModRegistries.STRATAGEM_ACTION);

    public static final StratagemActionType EMPTY = register("reinforce", EmptyAction.CODEC);
    public static final StratagemActionType REINFORCE = register("reinforce", ReinforceAction.CODEC);
    public static final StratagemActionType SPAWN_ITEM = register("spawn_item", SpawnItemAction.CODEC);
    public static final StratagemActionType SPAWN_SUPPLY = register("spawn_supply", SpawnSupplyAction.CODEC);
    public static final StratagemActionType SPAWN_BOMB = register("spawn_bomb", SpawnBombAction.CODEC);

    private static StratagemActionType register(String name, MapCodec<? extends StratagemAction> codec)
    {
        return Registry.register(ModBuiltInRegistries.STRATAGEM_ACTION_TYPE, StratagemsMod.id(name), new StratagemActionType(codec));
    }
}