package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.action.ReinforceAction;
import com.stevekung.stratagems.action.SpawnBombAction;
import com.stevekung.stratagems.action.SpawnItemAction;
import com.stevekung.stratagems.action.SpawnSupplyAction;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.api.references.ModBuiltInRegistries;
import com.stevekung.stratagems.api.references.StratagemActions;

import net.minecraft.core.Registry;

public class StratagemActionTypes
{
    public static final StratagemActionType REINFORCE = new StratagemActionType(ReinforceAction.CODEC);
    public static final StratagemActionType SPAWN_ITEM = new StratagemActionType(SpawnItemAction.CODEC);
    public static final StratagemActionType SPAWN_SUPPLY = new StratagemActionType(SpawnSupplyAction.CODEC);
    public static final StratagemActionType SPAWN_BOMB = new StratagemActionType(SpawnBombAction.CODEC);

    public static void init()
    {
        register("empty", StratagemActions.EMPTY);
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