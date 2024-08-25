package com.stevekung.stratagems.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface ModConstants
{
    Map<Holder<Stratagem>, StratagemInstance> CLIENT_SERVER_STRATAGEM_LIST = Maps.newLinkedHashMap();
    Map<Character, Character> WASD_TO_ARROWS = Map.of('w', '↑', 'a', '←', 's', '↓', 'd', '→');
    String MOD_ID = "stratagems";
    Logger LOGGER = LoggerFactory.getLogger("Stratagems");

    static String charToArrow(char code)
    {
        return WASD_TO_ARROWS.getOrDefault(code, '↑').toString();
    }

    static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    interface KeyBindings
    {
        String CATEGORY = "key.categories.stratagems";

        String STRATAGEM_MENU = "key.open_stratagem_menu";
        String STRATAGEM_UP = "key.stratagem_up";
        String STRATAGEM_DOWN = "key.stratagem_down";
        String STRATAGEM_LEFT = "key.stratagem_left";
        String STRATAGEM_RIGHT = "key.stratagem_right";
    }

    interface Packets
    {
        ResourceLocation SPAWN_STRATAGEM = id("spawn_stratagem");
        ResourceLocation USE_REPLENISH_STRATAGEM = id("use_replenish_stratagem");
        ResourceLocation SET_PLAYER_STRATAGEMS = id("set_player_stratagems");
        ResourceLocation SET_SERVER_STRATAGEMS = id("set_server_stratagems");
        ResourceLocation UPDATE_STRATAGEM = id("update_stratagem");
        ResourceLocation CLEAR_STRATAGEMS = id("clear_stratagems");
    }

    interface StratagemTag
    {
        TagKey<Stratagem> TNT_REPLENISH = TagKey.create(ModRegistries.STRATAGEM, id("tnt_replenish"));
    }

    interface Tag
    {
        String VARIANT = "variant";

        String STRATAGEM = "stratagem";
        String STRATAGEMS = "stratagems";
        String TICK = "tick";
        String NEXT_AVAILABLE_STRATAGEM_ID = "next_available_stratagem_id";
        String ID = "id";
        String INBOUND_DURATION = "inbound_duration";
        String DURATION = "duration";
        String COOLDOWN = "cooldown";
        String MAX_USE = "max_use";
        String STATE = "state";
        String SIDE = "side";
        String SHOULD_DISPLAY = "should_display";
    }
}