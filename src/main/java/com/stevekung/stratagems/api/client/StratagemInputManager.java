package com.stevekung.stratagems.api.client;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.references.StratagemSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class StratagemInputManager
{
    private static StratagemInputManager INSTANCE;

    private boolean menuOpen;
    private String inputCode = "";
    @Nullable
    private ClientStratagemInstance selected;

    private final Minecraft minecraft;

    private StratagemInputManager()
    {
        this.minecraft = Minecraft.getInstance();
    }

    public static StratagemInputManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new StratagemInputManager();
        }
        return INSTANCE;
    }

    public static boolean noneMatch(String inputCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(player)).noneMatch(instance -> instance.getCode().startsWith(inputCode));
    }

    public static Optional<ClientStratagemInstance> getInstanceFromCode(String inputCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(player) && instance.getCode().equals(inputCode)).findFirst();
    }

    public boolean isMenuOpen()
    {
        return this.menuOpen;
    }

    public void setMenuOpen(boolean menuOpen)
    {
        this.menuOpen = menuOpen;
    }

    public boolean hasInputCode()
    {
        return StringUtils.isNotEmpty(this.inputCode);
    }

    public String getInputCode()
    {
        return this.inputCode;
    }

    public void clearInputCode()
    {
        this.inputCode = "";
    }

    public void appendInputCode(String inputCode)
    {
        this.inputCode += inputCode;
    }

    public void clearSelected()
    {
        this.selected = null;
        this.minecraft.getSoundManager().stop(StratagemSounds.STRATAGEM_SELECT.getLocation(), SoundSource.PLAYERS);
    }

    public boolean hasSelected()
    {
        return this.selected != null;
    }

    @Nullable
    public ClientStratagemInstance getSelected()
    {
        return this.selected;
    }

    public void setSelected(ClientStratagemInstance selected)
    {
        this.selected = selected;
    }

    public static List<ClientStratagemInstance> all(Player player)
    {
        var allList = Lists.newArrayList(Iterables.concat(Ordering.natural().sortedCopy(ModConstants.CLIENT_SERVER_STRATAGEM_LIST.values()), Ordering.natural().sortedCopy(player.stratagemsData().listInstances())));
        return allList.stream().map(ClientStratagemInstance.class::cast).toList();
    }
}