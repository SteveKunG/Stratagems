package com.stevekung.stratagems.rule;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.registry.StratagemRules;
import net.minecraft.world.entity.player.Player;

public class ReplenishStratagemRule implements StratagemRule
{
    public static final MapCodec<ReplenishStratagemRule> CODEC = MapCodec.unit(new ReplenishStratagemRule());
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public StratagemRuleType getType()
    {
        return StratagemRules.REPLENISH;
    }

    @Override
    public boolean canUse(StratagemInstance instance, Player player)
    {
        return instance.isReady();
    }

    @Override
    public void onUse(StratagemInstance instance, Player player)
    {
        var stratagemData = player.getPlayerStratagems();
        var rearmProperties = instance.stratagem().properties();
        var count = 0;

        if (rearmProperties.replenish().isPresent() && rearmProperties.replenish().get().toReplenish().isPresent())
        {
            for (var toReplenishinstance : stratagemData.values().stream().filter(toReplenishinstance -> rearmProperties.replenish().get().toReplenish().get().contains(toReplenishinstance.getStratagem())).toList())
            {
                toReplenishinstance.state = StratagemState.COOLDOWN;

                var replenishedProperties = toReplenishinstance.stratagem().properties();
                toReplenishinstance.inboundDuration = replenishedProperties.inboundDuration();

                if (replenishedProperties.duration().isPresent())
                {
                    toReplenishinstance.duration = replenishedProperties.duration().get();
                }

                // replenished cooldown from rearm
                toReplenishinstance.cooldown = rearmProperties.cooldown();

                if (replenishedProperties.remainingUse().isPresent())
                {
                    toReplenishinstance.remainingUse = replenishedProperties.remainingUse().get();
                }

                LOGGER.info("Replenished {} stratagem!", toReplenishinstance.stratagem().name().getString());

                // Remove this rearm stratagem
                stratagemData.remove(instance.getStratagem());
                LOGGER.info("Remove {} replenisher stratagem!", instance.stratagem().name().getString());
                count++;
            }

            if (rearmProperties.replenish().get().replenishSound().isPresent() && count > 0)
            {
                player.playSound(rearmProperties.replenish().get().replenishSound().get(), 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void onReset(StratagemInstance instance, Player player)
    {
        player.getPlayerStratagems().remove(instance.getStratagem());
        LOGGER.info("Remove {} replenisher stratagem on reset!", instance.stratagem().name().getString());
    }

    @Override
    public void tick(StratagemInstance instance, Player player)
    {
    }

    public static Builder defaultRule()
    {
        return ReplenishStratagemRule::new;
    }
}