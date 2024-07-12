package com.stevekung.stratagems;

import java.util.Map;

import com.google.common.collect.Maps;
import net.minecraft.Util;

public interface ModConstants
{
    Map<Character, Character> WASD_TO_ARROWS = Util.make(Maps.newHashMap(), map ->
    {
        map.put('w', '↑');
        map.put('a', '←');
        map.put('s', '↓');
        map.put('d', '→');
    });

    static String charToArrow(char code)
    {
        return WASD_TO_ARROWS.getOrDefault(code, '↑').toString();
    }

    interface KeyBindings
    {
        String CATEGORY = "key.categories.stratagems";

        String STRATAGEMS_MENU = "key.open_stratagems_menu";
        String STRATAGEMS_UP = "key.stratagems_up";
        String STRATAGEMS_DOWN = "key.stratagems_down";
        String STRATAGEMS_LEFT = "key.stratagems_left";
        String STRATAGEMS_RIGHT = "key.stratagems_right";
    }
}