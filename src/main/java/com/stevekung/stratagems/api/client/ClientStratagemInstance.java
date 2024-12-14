package com.stevekung.stratagems.api.client;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemState;

import net.minecraft.core.Holder;

public class ClientStratagemInstance extends StratagemInstance
{
    private String jammedName = "";
    private String randomizedCode = "";
    public float animationTime = -200f;
    public boolean visible;
    public boolean selected;

    public ClientStratagemInstance(int id, Holder<Stratagem> stratagem, int inboundDuration, int duration, int cooldown, int lastMaxCooldown, int maxUse, StratagemState state, Side side, boolean shouldDisplay, boolean randomize)
    {
        super(id, stratagem, inboundDuration, duration, cooldown, lastMaxCooldown, maxUse, state, side, shouldDisplay, randomize);

        if (StratagemInputManager.getInstance().isMenuOpen())
        {
            this.animationTime = 0f;
            this.visible = true;
        }
    }

    public String getJammedName()
    {
        return this.jammedName;
    }

    public void setJammedName(String jammedName)
    {
        this.jammedName = jammedName;
    }

    public String getRandomizedCode()
    {
        return this.randomizedCode;
    }

    public void setRandomizedCode(String randomizedCode)
    {
        this.randomizedCode = randomizedCode;
    }
}