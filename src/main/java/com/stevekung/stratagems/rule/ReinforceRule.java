package com.stevekung.stratagems.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemInstanceContext;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.StratagemRules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class ReinforceRule implements StratagemRule
{
    public static final MapCodec<ReinforceRule> CODEC = MapCodec.unit(new ReinforceRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.REINFORCE;
    }

    @Override
    public boolean canUse(StratagemInstanceContext context)
    {
        return context.instance().remainingUse > 0;
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        if (context.instance().remainingUse > 0)
        {
            if (context.player() instanceof ServerPlayer serverPlayer && serverPlayer.serverLevel().players().stream().anyMatch(LivingEntity::isDeadOrDying))
            {
                context.instance().remainingUse--;
                LOGGER.info("{} stratagem has remainingUse: {}", context.instance().stratagem().name().getString(), context.instance().remainingUse);
            }
            else
            {
                LOGGER.info("No player dead");
            }
        }
    }

    @Override
    public void tick(StratagemInstanceContext context)
    {
        var instance = context.instance();
        var player = context.player();

        if (instance.state != StratagemState.COOLDOWN && instance.remainingUse == 0)
        {
            instance.cooldown = instance.stratagem().properties().cooldown();
            instance.state = StratagemState.COOLDOWN;
            LOGGER.info("{} stratagem has no remaining use, cooldown: {}", instance.stratagem().name().getString(), instance.formatTickDuration(instance.cooldown, player));
        }

        if (instance.state == StratagemState.COOLDOWN)
        {
            if (instance.cooldown > 0)
            {
                instance.cooldown--;

                if (instance.cooldown % 20 == 0)
                {
                    LOGGER.info("{} stratagem has cooldown: {}", instance.stratagem().name().getString(), instance.formatTickDuration(instance.cooldown, player));
                }
            }

            if (instance.cooldown == 0)
            {
                instance.remainingUse++;
                LOGGER.info("{} stratagem switch state from {} to {} with remainingUse: {}", instance.stratagem().name().getString(), instance.state, StratagemState.READY, instance.remainingUse);
                instance.state = StratagemState.READY;
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReinforceRule::new;
    }
}