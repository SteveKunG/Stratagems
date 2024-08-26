package com.stevekung.stratagems.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;

public class NewDeathScreen extends Screen
{
    /**
     * The integer value containing the number of ticks that have passed since the player's death
     */
    private int delayTicker;
    private final Component causeOfDeath;
    private Component deathScore;
    private final List<Button> exitButtons = Lists.newArrayList();
    @Nullable
    private Button exitToTitleButton;

    public NewDeathScreen(@Nullable Component causeOfDeath)
    {
        super(Component.translatable("deathScreen.title"));
        this.causeOfDeath = causeOfDeath;
    }

    @Override
    protected void init()
    {
        this.delayTicker = 0;
        this.exitButtons.clear();
        Component component = Component.translatable("deathScreen.respawn");
        this.exitButtons.add(this.addRenderableWidget(Button.builder(component, button ->
        {
            this.minecraft.player.respawn();
            button.active = false;
        }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.exitToTitleButton = this.addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"), button -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true)).bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
        this.exitButtons.add(this.exitToTitleButton);
        this.setButtonsActive(false);
        this.deathScore = Component.translatable("deathScreen.score.value", Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == 256)
        {
            this.minecraft.setScreen(new PauseScreen(true));//TODO Set parent screen
            return true;
        }
        else if (keyCode == InputConstants.KEY_A)
        {
//            Minecraft.getInstance().getConnection().send(new ServerboundTeleportToEntityPacket(this.profile.getId()));
        }
        else if (keyCode == InputConstants.KEY_D)
        {

        }
        else
        {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private void handleExitToTitleScreen()
    {
        ConfirmScreen confirmScreen = new net.minecraft.client.gui.screens.DeathScreen.TitleConfirmScreen(bl ->
        {
            if (bl)
            {
                this.exitToTitleScreen();
            }
            else
            {
                this.minecraft.player.respawn();
                this.minecraft.setScreen(null);
            }
        }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
        this.minecraft.setScreen(confirmScreen);
        confirmScreen.setDelay(20);
    }

    private void exitToTitleScreen()
    {
        if (this.minecraft.level != null)
        {
            this.minecraft.level.disconnect();
        }

        this.minecraft.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 16777215);
        guiGraphics.pose().popPose();
        if (this.causeOfDeath != null)
        {
            guiGraphics.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
        }

        guiGraphics.drawCenteredString(this.font, this.deathScore, this.width / 2, 100, 16777215);
        if (this.causeOfDeath != null && mouseY > 85 && mouseY < 85 + 9)
        {
            var style = this.getClickedComponentStyleAt(mouseX);
            guiGraphics.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        renderDeathBackground(guiGraphics, this.width, this.height);
    }

    static void renderDeathBackground(GuiGraphics guiGraphics, int width, int height)
    {
        guiGraphics.fillGradient(0, 0, width, height, 1615855616, -1602211792);
    }

    @Nullable
    private Style getClickedComponentStyleAt(int x)
    {
        if (this.causeOfDeath == null)
        {
            return null;
        }
        else
        {
            var i = this.minecraft.font.width(this.causeOfDeath);
            var j = this.width / 2 - i / 2;
            var k = this.width / 2 + i / 2;
            return x >= j && x <= k ? this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, x - j) : null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.causeOfDeath != null && mouseY > 85.0 && mouseY < (double) (85 + 9))
        {
            var style = this.getClickedComponentStyleAt((int) mouseX);
            if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == Action.OPEN_URL)
            {
                this.handleComponentClicked(style);
                return false;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void tick()
    {
        super.tick();
        this.delayTicker++;
        if (this.delayTicker == 20)
        {
            this.setButtonsActive(true);
        }
    }

    private void setButtonsActive(boolean active)
    {
        for (var button : this.exitButtons)
        {
            button.active = active;
        }
    }
}
