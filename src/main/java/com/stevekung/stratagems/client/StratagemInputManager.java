package com.stevekung.stratagems.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.registry.StratagemSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class StratagemInputManager
{
    private static final StratagemInputManager INSTANCE = new StratagemInputManager();

    private boolean stratagemsMenuOpen;
    private String tempStratagemCode = "";
    private String selectedStratagemCode;
    private ResourceKey<Stratagem> selectedStratagem;
    private StratagemInstance.Side side;

    private final Minecraft minecraft;

    private StratagemInputManager()
    {
        this.minecraft = Minecraft.getInstance();
    }

    public static StratagemInputManager getInstance()
    {
        return INSTANCE;
    }

    public static boolean noneMatch(String tempStratagemCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(null, player)).noneMatch(instance -> instance.getCode().startsWith(tempStratagemCode));
    }

    public static boolean foundMatch(String tempStratagemCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(null, player)).anyMatch(instance -> instance.getCode().equals(tempStratagemCode));
    }

    public static StratagemInstance getInstanceFromCode(String tempStratagemCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(null, player) && instance.getCode().equals(tempStratagemCode)).findFirst().get();
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
        this.side = null;
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

    public StratagemInstance.Side getSide()
    {
        return this.side;
    }

    public void setSide(StratagemInstance.Side side)
    {
        this.side = side;
    }

    public static List<StratagemInstance> all(Player player)
    {
        return Lists.newArrayList(Iterables.concat(StratagemsClientMod.CLIENT_STRATAGEM_LIST, player.getStratagems().values()));
    }
}