package com.stevekung.stratagems.action;

public interface StratagemAction
{
    StratagemActionType getType();

    void action(StratagemActionContext context);

    @FunctionalInterface
    interface Builder
    {
        StratagemAction build();
    }
}