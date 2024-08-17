package com.stevekung.stratagems.api.client;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.references.StratagemSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class StratagemInputManager
{
    private static final StratagemInputManager INSTANCE = new StratagemInputManager();

    private boolean menuOpen;
    private String inputCode = "";
    private String selectedCode;
    private ResourceKey<Stratagem> selected;
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

    public static boolean noneMatch(String inputCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(null, player)).noneMatch(instance -> instance.getCode().startsWith(inputCode));
    }

    public static Optional<StratagemInstance> foundMatchFirst(String inputCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(null, player) && instance.getCode().equals(inputCode)).findFirst();
    }

    public static StratagemInstance getInstanceFromCode(String inputCode, Player player)
    {
        return all(player).stream().filter(instance -> instance.canUse(null, player) && instance.getCode().equals(inputCode)).findFirst().get();
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

    public String getSelectedCode()
    {
        return this.selectedCode;
    }

    public boolean hasSelectedCode()
    {
        return StringUtils.isNotEmpty(this.selectedCode);
    }

    public void setSelectedCode(String selectedCode)
    {
        this.selectedCode = selectedCode;
    }

    public void clearCode()
    {
        this.selectedCode = null;
        this.selected = null;
        this.side = null;
        this.minecraft.getSoundManager().stop(StratagemSounds.STRATAGEM_SELECT.getLocation(), SoundSource.PLAYERS);
    }

    public boolean hasSelected()
    {
        return this.selected != null;
    }

    public ResourceKey<Stratagem> getSelected()
    {
        return this.selected;
    }

    public void setSelected(ResourceKey<Stratagem> selected)
    {
        this.selected = selected;
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
        return Lists.newArrayList(Iterables.concat(Ordering.natural().sortedCopy(ModConstants.CLIENT_SERVER_STRATAGEM_LIST.values()), Ordering.natural().sortedCopy(player.getStratagems().values())));
    }
}