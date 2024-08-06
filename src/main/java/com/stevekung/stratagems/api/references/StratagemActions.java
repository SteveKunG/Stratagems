package com.stevekung.stratagems.api.references;

import com.mojang.serialization.Codec;
import com.stevekung.stratagems.action.ReinforceAction;
import com.stevekung.stratagems.action.SpawnBombAction;
import com.stevekung.stratagems.action.SpawnItemAction;
import com.stevekung.stratagems.action.SpawnSupplyAction;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.action.EmptyAction;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionType;

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

    public static final StratagemActionType EMPTY = new StratagemActionType(EmptyAction.CODEC);
    public static final StratagemActionType REINFORCE = new StratagemActionType(ReinforceAction.CODEC);
    public static final StratagemActionType SPAWN_ITEM = new StratagemActionType(SpawnItemAction.CODEC);
    public static final StratagemActionType SPAWN_SUPPLY = new StratagemActionType(SpawnSupplyAction.CODEC);
    public static final StratagemActionType SPAWN_BOMB = new StratagemActionType(SpawnBombAction.CODEC);

    public static void init()
    {
        register("empty", EMPTY);
        register("reinforce", REINFORCE);
        register("spawn_item", SPAWN_ITEM);
        register("spawn_supply", SPAWN_SUPPLY);
        register("spawn_bomb", SPAWN_BOMB);
    }

    private static void register(String name, StratagemActionType type)
    {
        Registry.register(ModBuiltInRegistries.STRATAGEM_ACTION_TYPE, ModConstants.id(name), type);
    }
}