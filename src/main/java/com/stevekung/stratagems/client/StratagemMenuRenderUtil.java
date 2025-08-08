package com.stevekung.stratagems.client;

import net.minecraft.client.gui.GuiGraphics;

public class StratagemMenuRenderUtil
{
    public static void renderBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, boolean menuOpen)
    {
        var i = x - 3;
        var j = y - 3;
        var k = width + 3 + 3;
        var l = height + 3 + 3;
        renderRectangle(guiGraphics, i, j, k, l, color);

        if (menuOpen)
        {
            renderVerticalLine(guiGraphics, i - 5, j, l, color);
        }
    }

    private static void renderVerticalLine(GuiGraphics guiGraphics, int x, int y, int length, int color)
    {
        guiGraphics.fill(x, y, x + 1, y + length, color);
    }

    private static void renderRectangle(GuiGraphics guiGraphics, int x, int y, int width, int height, int color)
    {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}