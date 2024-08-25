package com.stevekung.stratagems.client;

import net.minecraft.client.gui.GuiGraphics;

public class StratagemMenuRenderUtil
{
    public static void renderBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int color)
    {
        int i = x - 3;
        int j = y - 3;
        int k = width + 3 + 3;
        int l = height + 3 + 3;
        renderRectangle(guiGraphics, i, j, k, l, z, color);
        renderVerticalLine(guiGraphics, i - 5, j, l, z, color);
    }

    private static void renderVerticalLine(GuiGraphics guiGraphics, int x, int y, int length, int z, int color)
    {
        guiGraphics.fill(x, y, x + 1, y + length, z, color);
    }

    private static void renderRectangle(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int color)
    {
        guiGraphics.fill(x, y, x + width, y + height, z, color);
    }
}