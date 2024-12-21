package com.stevekung.stratagems.datagen;

import java.util.Optional;

import com.stevekung.stratagems.action.SpawnItemAction;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemDisplay;
import com.stevekung.stratagems.api.StratagemProperties;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.rule.DefaultRule;
import com.stevekung.stratagems.api.rule.DepletedRule;
import com.stevekung.stratagems.api.rule.StratagemRule;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class EnderDragonStratagems
{
    public static final ResourceKey<Stratagem> IRON_PICKAXE = createKey("iron_pickaxe");
    public static final ResourceKey<Stratagem> IRON_AXE = createKey("iron_axe");
    public static final ResourceKey<Stratagem> IRON_SHOVEL = createKey("iron_shovel");
    public static final ResourceKey<Stratagem> IRON_SWORD = createKey("iron_sword");
    public static final ResourceKey<Stratagem> CRAFTING_TABLE = createKey("crafting_table");
    public static final ResourceKey<Stratagem> FURNACE = createKey("furnace");

    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        register(context, IRON_PICKAXE, "saswwd", Items.IRON_PICKAXE, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_PICKAXE)), DepletedRule.defaultRule(), StratagemProperties.withDepleted(100, 1200, 5, ModConstants.BLUE_BEAM_COLOR));
        register(context, IRON_AXE, "sdswwd", Items.IRON_AXE, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_AXE)), DepletedRule.defaultRule(), StratagemProperties.withDepleted(100, 1200, 5, ModConstants.BLUE_BEAM_COLOR));
        register(context, IRON_SHOVEL, "ssdwwd", Items.IRON_SHOVEL, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SHOVEL)), DepletedRule.defaultRule(), StratagemProperties.withDepleted(100, 1200, 5, ModConstants.BLUE_BEAM_COLOR));
        register(context, IRON_SWORD, "ssawds", Items.IRON_SWORD, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SWORD)), DepletedRule.defaultRule(), StratagemProperties.withDepleted(100, 1200, 5, ModConstants.BLUE_BEAM_COLOR));
        register(context, CRAFTING_TABLE, "swws", Items.CRAFTING_TABLE, SpawnItemAction.spawnItem(new ItemStack(Items.CRAFTING_TABLE)), StratagemProperties.simple(60, 600, ModConstants.BLUE_BEAM_COLOR));
        register(context, FURNACE, "wssw", Items.FURNACE, SpawnItemAction.spawnItem(new ItemStack(Items.FURNACE)), StratagemProperties.simple(60, 600, ModConstants.BLUE_BEAM_COLOR));
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemLike icon, StratagemAction.Builder action, StratagemRule.Builder rule, StratagemProperties properties)
    {
        register(context, key, code, new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(icon)), Optional.empty(), Optional.empty(), true, Optional.empty()), action, rule, properties);
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, StratagemDisplay display, StratagemAction.Builder action, StratagemProperties properties)
    {
        register(context, key, code, display, action, DefaultRule.defaultRule(), properties);
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemLike icon, StratagemAction.Builder action, StratagemProperties properties)
    {
        register(context, key, code, new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(icon)), Optional.empty(), Optional.empty(), true, Optional.empty()), action, DefaultRule.defaultRule(), properties);
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, StratagemDisplay display, StratagemAction.Builder action, StratagemRule.Builder rule, StratagemProperties properties)
    {
        context.register(key, new Stratagem(code, Component.translatable(key.location().toLanguageKey("stratagem")), display, action.build(), rule.build(), properties));
    }

    private static ResourceKey<Stratagem> createKey(String name)
    {
        return ResourceKey.create(ModRegistries.STRATAGEM, ModConstants.id(name));
    }
}