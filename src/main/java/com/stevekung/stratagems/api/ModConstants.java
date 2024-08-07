package com.stevekung.stratagems.api;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.stevekung.stratagems.api.references.ModRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface ModConstants
{
    List<StratagemInstance> CLIENT_SERVER_STRATAGEM_LIST = Lists.newCopyOnWriteArrayList();
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
        ResourceLocation UPDATE_PLAYER_STRATAGEMS = id("update_player_stratagems");
        ResourceLocation UPDATE_SERVER_STRATAGEMS = id("update_server_stratagems");
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
        String INBOUND_DURATION = "inbound_duration";
        String DURATION = "duration";
        String COOLDOWN = "cooldown";
        String REMAINING_USE = "remaining_use";
        String STATE = "state";
        String SIDE = "side";
    }
}