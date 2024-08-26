package com.stevekung.stratagems.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public class ModGameRules
{
    public static final GameRules.Key<GameRules.BooleanValue> REINFORCE_ON_DEATH = register("reinforceOnDeath", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type)
    {
        return GameRuleRegistry.register(name, category, type);
    }
}