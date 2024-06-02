package com.stevekung.stratagems;

import org.apache.commons.lang3.StringUtils;

public class StratagemManager
{
    private static final StratagemManager INSTANCE = new StratagemManager();

    private boolean stratagemsMenuOpen;
    private String tempStratagemCode = "";
    private String selectedStratagemCode;
    private Stratagem selectedStratagem;

    public static StratagemManager getInstance()
    {
        return INSTANCE;
    }

    public boolean isStratagemsMenuOpen()
    {
        return this.stratagemsMenuOpen;
    }

    public void setStratagemsMenuOpen(boolean stratagemsMenuOpen)
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
    }

    public boolean hasSelectedStratagem()
    {
        return this.selectedStratagem != null;
    }

    public Stratagem getSelectedStratagem()
    {
        return this.selectedStratagem;
    }

    public void setSelectedStratagem(Stratagem selectedStratagem)
    {
        this.selectedStratagem = selectedStratagem;
    }
}