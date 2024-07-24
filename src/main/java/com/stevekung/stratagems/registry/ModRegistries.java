package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.action.StratagemAction;
import com.stevekung.stratagems.action.StratagemActionType;
import com.stevekung.stratagems.rule.StratagemRule;
import com.stevekung.stratagems.rule.StratagemRuleType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries
{
    public static final ResourceKey<Registry<Stratagem>> STRATAGEM = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("stratagem"));
    public static final ResourceKey<Registry<StratagemAction>> STRATAGEM_ACTION = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("stratagem_action"));
    public static final ResourceKey<Registry<StratagemActionType>> STRATAGEM_ACTION_TYPE = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("stratagem_action_type"));
    public static final ResourceKey<Registry<StratagemRule>> STRATAGEM_RULE = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("stratagem_rule"));
    public static final ResourceKey<Registry<StratagemRuleType>> STRATAGEM_RULE_TYPE = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("stratagem_rule_type"));
}