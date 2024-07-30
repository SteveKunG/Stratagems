package com.stevekung.stratagems;

import org.apache.commons.lang3.StringUtils;
import com.stevekung.stratagems.registry.StratagemSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;

public class StratagemMenuManager
{
    private static final StratagemMenuManager INSTANCE = new StratagemMenuManager();

    private boolean stratagemsMenuOpen;
    private String tempStratagemCode = "";
    private String selectedStratagemCode;
    private ResourceKey<Stratagem> selectedStratagem;

    private final Minecraft minecraft;

    private StratagemMenuManager()
    {
        this.minecraft = Minecraft.getInstance();
    }

    public static StratagemMenuManager getInstance()
    {
        return INSTANCE;
    }

    public boolean isMenuOpen()
    {
        return this.stratagemsMenuOpen;
    }

    public void setMenuOpen(boolean stratagemsMenuOpen)
    {
        this.stratagemsMenuOpen = stratagemsMenuOpen;
    }

    public boolean hasTempStratagemCode()
    {
        return StringUtils.isNotEmpty(this.tempStratagemCode);
    }

    public String getTempStratagemCode()
    {
        return this.tempStratagemCode;
    }

    public void clearTempStratagemCode()
    {
        this.tempStratagemCode = "";
    }

    public void appendTempStratagemCode(String tempStratagemCode)
    {
        this.tempStratagemCode += tempStratagemCode;
    }

    public String getSelectedStratagemCode()
    {
        return this.selectedStratagemCode;
    }

    public boolean hasSelectedStratagemCode()
    {
        return StringUtils.isNotEmpty(this.selectedStratagemCode);
    }

    public void setSelectedStratagemCode(String selectedStratagemCode)
    {
        this.selectedStratagemCode = selectedStratagemCode;
    }

    public void clearStratagemCode()
    {
        this.selectedStratagemCode = null;
        this.selectedStratagem = null;
        this.minecraft.getSoundManager().stop(StratagemSounds.STRATAGEM_SELECT.getLocation(), SoundSource.PLAYERS);
    }

    public boolean hasSelectedStratagem()
    {
        return this.selectedStratagem != null;
    }

    public ResourceKey<Stratagem> getSelectedStratagem()
    {
        return this.selectedStratagem;
    }

    public void setSelectedStratagem(ResourceKey<Stratagem> selectedStratagem)
    {
        this.selectedStratagem = selectedStratagem;
    }
}