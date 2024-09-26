package com.stevekung.stratagems.api.client;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemState;

import net.minecraft.core.Holder;

public class ClientStratagemInstance extends StratagemInstance
{
    private String jammedName = "";

    public ClientStratagemInstance(int id, Holder<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int lastMaxCooldown, int maxUse, StratagemState state, Side side, boolean shouldDisplay)
    {
        super(id, stratagem, inboundDuration, duration, cooldown, lastMaxCooldown, maxUse, state, side, shouldDisplay);
    }

    public String getJammedName()
    {
        return this.jammedName;
    }

    public void setJammedName(String jammedName)
    {
        this.jammedName = jammedName;
    }
}