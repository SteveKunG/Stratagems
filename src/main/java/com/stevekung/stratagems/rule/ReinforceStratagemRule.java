package com.stevekung.stratagems.rule;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.StratagemRules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ReinforceStratagemRule implements StratagemRule
{
    public static final MapCodec<ReinforceStratagemRule> CODEC = MapCodec.unit(new ReinforceStratagemRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.REINFORCE;
    }

    @Override
    public boolean canUse(StratagemInstance instance, Player player)
    {
        return instance.remainingUse > 0;
    }

    @Override
    public void onUse(StratagemInstance instance, Player player)
    {
        if (instance.remainingUse > 0)
        {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.serverLevel().players().stream().filter(playerx -> playerx.isDeadOrDying()).count() > 0)
            {
                instance.remainingUse--;
                LOGGER.info("{} stratagem has remainingUse: {}", instance.stratagem().name().getString(), instance.remainingUse);
            }
        }
    }

    @Override
    public void tick(StratagemInstance instance, Player player)
    {
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
        return ReinforceStratagemRule::new;
    }
}