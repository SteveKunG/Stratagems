package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.StratagemRules;
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
    public boolean canUse(StratagemEntry entry)
    {
        return entry.remainingUse > 0;
    }

    @Override
    public void onUse(StratagemEntry entry, Player player)
    {
        if (entry.remainingUse > 0)
        {
            entry.remainingUse--;
            LOGGER.info("{} stratagem has remainingUse: {}", entry.stratagem().name().getString(), entry.remainingUse);
        }
    }

    @Override
    public void tick(StratagemEntry entry)
    {
        if (entry.state != StratagemState.COOLDOWN && entry.remainingUse == 0)
        {
            entry.cooldown = entry.stratagem().properties().cooldown();
            entry.state = StratagemState.COOLDOWN;
            LOGGER.info("{} stratagem has no remaining use, cooldown: {}", entry.stratagem().name().getString(), entry.formatTickDuration(entry.cooldown));
        }

        if (entry.state == StratagemState.COOLDOWN)
        {
            if (entry.cooldown > 0)
            {
                entry.cooldown--;

                if (entry.cooldown % 20 == 0)
                {
                    LOGGER.info("{} stratagem has cooldown: {}", entry.stratagem().name().getString(), entry.formatTickDuration(entry.cooldown));
                }
            }

            if (entry.cooldown == 0)
            {
                entry.remainingUse++;
                LOGGER.info("{} stratagem switch state from {} to {} with remainingUse: {}", entry.stratagem().name().getString(), entry.state, StratagemState.READY, entry.remainingUse);
                entry.state = StratagemState.READY;
            }
        }
    }

    public static Builder defaultRule()
    {
        return ReinforceStratagemRule::new;
    }
}