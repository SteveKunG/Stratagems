package com.stevekung.stratagems.client;

import org.lwjgl.glfw.GLFW;
import com.stevekung.stratagems.ModConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class KeyBindings
{
    public static final KeyMapping OPEN_STRATAGEMS_MENU = new KeyMapping(ModConstants.KeyBindings.STRATAGEMS_MENU, GLFW.GLFW_KEY_Z, ModConstants.KeyBindings.CATEGORY);
    public static final KeyMapping STRATAGEMS_UP = new KeyMapping(ModConstants.KeyBindings.STRATAGEMS_UP, GLFW.GLFW_KEY_UP, ModConstants.KeyBindings.CATEGORY);
    public static final KeyMapping STRATAGEMS_DOWN = new KeyMapping(ModConstants.KeyBindings.STRATAGEMS_DOWN, GLFW.GLFW_KEY_DOWN, ModConstants.KeyBindings.CATEGORY);
    public static final KeyMapping STRATAGEMS_LEFT = new KeyMapping(ModConstants.KeyBindings.STRATAGEMS_LEFT, GLFW.GLFW_KEY_LEFT, ModConstants.KeyBindings.CATEGORY);
    public static final KeyMapping STRATAGEMS_RIGHT = new KeyMapping(ModConstants.KeyBindings.STRATAGEMS_RIGHT, GLFW.GLFW_KEY_RIGHT, ModConstants.KeyBindings.CATEGORY);

    public static void init()
    {
        KeyBindingHelper.registerKeyBinding(OPEN_STRATAGEMS_MENU);
        KeyBindingHelper.registerKeyBinding(STRATAGEMS_UP);
        KeyBindingHelper.registerKeyBinding(STRATAGEMS_DOWN);
        KeyBindingHelper.registerKeyBinding(STRATAGEMS_LEFT);
        KeyBindingHelper.registerKeyBinding(STRATAGEMS_RIGHT);
    }
}