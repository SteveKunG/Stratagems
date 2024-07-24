package com.stevekung.stratagems.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.rule.DefaultStratagemRule;
import com.stevekung.stratagems.rule.ReinforceStratagemRule;
import com.stevekung.stratagems.rule.StratagemRule;
import com.stevekung.stratagems.rule.StratagemRuleType;
import net.minecraft.core.Registry;

public class StratagemRules
{
    private static final Codec<StratagemRule> TYPED_CODEC = ModBuiltInRegistries.STRATAGEM_RULE_TYPE.byNameCodec().dispatch("type", StratagemRule::getType, StratagemRuleType::codec);
    public static final Codec<StratagemRule> CODEC = Codec.lazyInitialized(() -> TYPED_CODEC);

    public static final StratagemRuleType DEFAULT = register("default", DefaultStratagemRule.CODEC);
    public static final StratagemRuleType REINFORCE = register("reinforce", ReinforceStratagemRule.CODEC);

    private static StratagemRuleType register(String name, MapCodec<? extends StratagemRule> codec)
    {
        return Registry.register(ModBuiltInRegistries.STRATAGEM_RULE_TYPE, StratagemsMod.id(name), new StratagemRuleType(codec));
    }
}