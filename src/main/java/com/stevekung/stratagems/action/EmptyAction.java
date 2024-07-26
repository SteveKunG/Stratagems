package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;
import com.stevekung.stratagems.registry.StratagemActions;

public record EmptyAction() implements StratagemAction
{
    public static final MapCodec<EmptyAction> CODEC = MapCodec.unit(new EmptyAction());

    @Override
    public StratagemActionType getType()
    {
        return StratagemActions.EMPTY;
    }

    @Override
    public void action(StratagemActionContext context)
    {
    }

    public static Builder empty()
    {
        return EmptyAction::new;
    }
}