package com.stevekung.stratagems.action;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.api.references.StratagemActions;

import net.minecraft.world.entity.LivingEntity;

public record ReinforceAction() implements StratagemAction
{
    public static final MapCodec<ReinforceAction> CODEC = MapCodec.unit(new ReinforceAction());

    @Override
    public StratagemActionType getType()
    {
        return StratagemActions.REINFORCE;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        //TODO Temp
        var level = context.level();
        var optional = level.players().stream().filter(LivingEntity::isDeadOrDying).flatMap(serverPlayer -> Optional.of(serverPlayer).stream()).findFirst();
        optional.ifPresent(level::addRespawnedPlayer);
    }

    public static Builder reinforce()
    {
        return ReinforceAction::new;
    }
}