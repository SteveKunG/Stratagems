package com.stevekung.stratagems.api.action;

public interface StratagemAction
{
    /**
     * @return type of stratagem action
     */
    StratagemActionType getType();

    /**
     * Create an action for a stratagem which interacts in the world, such as spawning hellpod, entity, items, etc.
     *
     * @param context the stratagem action context
     */
    void action(StratagemActionContext context);

    @FunctionalInterface
    interface Builder
    {
        StratagemAction build();
    }
}