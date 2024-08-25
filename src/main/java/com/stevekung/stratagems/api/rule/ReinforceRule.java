package com.stevekung.stratagems.api.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.StratagemInstanceContext;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.references.StratagemRules;
import com.stevekung.stratagems.api.util.StratagemUtils;

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
        return context.instance().maxUse > 0;
    }

    @Override
    public void onUse(StratagemInstanceContext context)
    {
        if (context.instance().maxUse > 0)
        {
            if (context.player() instanceof ServerPlayer serverPlayer && serverPlayer.serverLevel().players().stream().anyMatch(LivingEntity::isDeadOrDying))
            {
                context.instance().maxUse--;
                LOGGER.info("{} stratagem has maxUse: {}", context.instance().stratagem().name().getString(), context.instance().maxUse);
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
        var level = player != null ? player.level() : context.server().overworld();

        if (instance.state != StratagemState.COOLDOWN && instance.maxUse == 0)
        {
            instance.cooldown = instance.stratagem().properties().cooldown();
            instance.lastMaxCooldown = instance.cooldown;
            instance.state = StratagemState.COOLDOWN;
            LOGGER.info("{} stratagem has no remaining use, cooldown: {}", instance.stratagem().name().getString(), StratagemUtils.formatTickDuration(instance.cooldown, level));
        }

        if (instance.state == StratagemState.COOLDOWN)
        {
            if (instance.cooldown > 0)
            {
                instance.cooldown--;

                if (instance.cooldown % 20 == 0)
                {
                    LOGGER.info("{} stratagem has cooldown: {}", instance.stratagem().name().getString(), StratagemUtils.formatTickDuration(instance.cooldown, level));
                }
            }

            if (instance.cooldown == 0)
            {
                instance.maxUse++;
                LOGGER.info("{} stratagem switch state from {} to {} with maxUse: {}", instance.stratagem().name().getString(), instance.state, StratagemState.READY, instance.maxUse);
                instance.state = StratagemState.READY;
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReinforceRule::new;
    }
}