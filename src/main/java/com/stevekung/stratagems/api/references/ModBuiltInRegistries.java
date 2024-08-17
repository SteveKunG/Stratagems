package com.stevekung.stratagems.api.references;

import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.api.rule.StratagemRuleType;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModBuiltInRegistries
{
    public static final Registry<StratagemActionType> STRATAGEM_ACTION_TYPE = BuiltInRegistries.registerSimple(ModRegistries.STRATAGEM_ACTION_TYPE, registry -> StratagemActions.EMPTY);
    public static final Registry<StratagemRuleType> STRATAGEM_RULE_TYPE = BuiltInRegistries.registerSimple(ModRegistries.STRATAGEM_RULE_TYPE, registry -> StratagemRules.DEFAULT);
}