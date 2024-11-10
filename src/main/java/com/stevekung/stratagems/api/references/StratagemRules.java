package com.stevekung.stratagems.api.references;

import com.mojang.serialization.Codec;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.rule.*;

import net.minecraft.core.Registry;

public class StratagemRules
{
    private static final Codec<StratagemRule> TYPED_CODEC = ModBuiltInRegistries.STRATAGEM_RULE_TYPE.byNameCodec().dispatch("type", StratagemRule::getType, StratagemRuleType::codec);
    public static final Codec<StratagemRule> CODEC = Codec.lazyInitialized(() -> TYPED_CODEC);

    public static final StratagemRuleType DEFAULT = new StratagemRuleType(DefaultRule.CODEC);
    public static final StratagemRuleType REINFORCE = new StratagemRuleType(ReinforceRule.CODEC);
    public static final StratagemRuleType DEPLETED = new StratagemRuleType(DepletedRule.CODEC);
    public static final StratagemRuleType DEPLETED_REARM = new StratagemRuleType(DepletedAndRearmRule.CODEC);
    public static final StratagemRuleType REPLENISH = new StratagemRuleType(ReplenishRule.CODEC);

    public static void init()
    {
        register("default", DEFAULT);
        register("reinforce", REINFORCE);
        register("depleted", DEPLETED);
        register("depleted_rearm", DEPLETED_REARM);
        register("replenish", REPLENISH);
    }

    private static void register(String name, StratagemRuleType type)
    {
        Registry.register(ModBuiltInRegistries.STRATAGEM_RULE_TYPE, ModConstants.id(name), type);
    }
}