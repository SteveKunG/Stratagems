package com.stevekung.stratagems.registry;

import java.util.Optional;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.stevekung.stratagems.action.ReinforceAction;
import com.stevekung.stratagems.action.SpawnBombAction;
import com.stevekung.stratagems.action.SpawnItemAction;
import com.stevekung.stratagems.action.SpawnSupplyAction;
import com.stevekung.stratagems.api.*;
import com.stevekung.stratagems.api.action.EmptyAction;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.rule.*;

import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class Stratagems
{
    public static final ResourceKey<Stratagem> REINFORCE = createKey("reinforce");
    public static final ResourceKey<Stratagem> BOW = createKey("bow");
    public static final ResourceKey<Stratagem> SUPPLY_CHEST = createKey("supply_chest");
    public static final ResourceKey<Stratagem> IRON_SWORD = createKey("iron_sword");
    public static final ResourceKey<Stratagem> IRON_PICKAXE = createKey("iron_pickaxe");
    public static final ResourceKey<Stratagem> BLOCK = createKey("block");
    public static final ResourceKey<Stratagem> TNT = createKey("tnt");
    public static final ResourceKey<Stratagem> FAST_TNT = createKey("fast_tnt");
    public static final ResourceKey<Stratagem> LONG_TNT = createKey("long_tnt");
    public static final ResourceKey<Stratagem> TNT_REARM = createKey("tnt_rearm");

    private static final int BLUE_COLOR = FastColor.ARGB32.color(115, 215, 255);
    private static final int RED_COLOR = FastColor.ARGB32.color(255, 60, 60);

    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        register(context, REINFORCE, "wsdaw", new StratagemDisplay(StratagemDisplay.Type.PLAYER_ICON, Optional.empty(), Optional.empty(), Optional.of(new ResolvableProfile(Optional.empty(), Optional.empty(), Util.make(new PropertyMap(), map -> map.put("name", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNzZjYzIyZTdjMmFiOWM1NDBkMTI0NGVhZGJhNTgxZjVkZDllMThmOWFkYWNmMDUyODBhNWI0OGI4ZjYxOCJ9fX0"))))), true, Optional.empty()), ReinforceAction.reinforce(), ReinforceRule.defaultRule(), StratagemProperties.withDepleted(0, 2400, 20, BLUE_COLOR));
    }

    public static void bootstrapTest(BootstrapContext<Stratagem> context)
    {
        register(context, BOW, "ssawd", Items.BOW, SpawnItemAction.spawnItems(new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64)), StratagemProperties.simple(100, 6000, BLUE_COLOR));
        register(context, SUPPLY_CHEST, "sswd", new StratagemDisplay(StratagemDisplay.Type.TEXTURE, Optional.empty(), Optional.of(ModConstants.id("textures/stratagem/supply_chest.png")), Optional.empty(), true, Optional.empty()), SpawnSupplyAction.spawnSupply(BuiltInLootTables.SPAWN_BONUS_CHEST), DefaultRule.defaultRule(), StratagemProperties.simple(200, 6000, BLUE_COLOR));
        register(context, IRON_SWORD, "saswd", Items.IRON_SWORD, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SWORD)), StratagemProperties.simple(100, 6000, BLUE_COLOR));
        register(context, IRON_PICKAXE, "saswwd", Items.IRON_PICKAXE, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_PICKAXE)), StratagemProperties.simple(200, 6000, BLUE_COLOR));
        register(context, BLOCK, "ssss", new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(Items.STONE)), Optional.empty(), Optional.empty(), false, Optional.of("64")), SpawnItemAction.spawnItem(new ItemStack(Items.STONE, 64)), StratagemProperties.simple(100, 1200, BLUE_COLOR));
        register(context, TNT, "wdsd", Items.TNT, SpawnBombAction.spawnBomb(), DepletedRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 3, RED_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), "tnt", Optional.empty(), Optional.empty())));
        register(context, FAST_TNT, "wdsw", new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(Items.TNT)), Optional.empty(), Optional.empty(), false, Optional.of("F")), SpawnBombAction.spawnBomb(40), DepletedRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 3, RED_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), "tnt", Optional.empty(), Optional.empty())));
        register(context, LONG_TNT, "awdw", Items.TNT, SpawnBombAction.spawnBomb(100, Blocks.DIAMOND_BLOCK.defaultBlockState()), DepletedRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 1, RED_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), "tnt", Optional.empty(), Optional.empty())));
        register(context, TNT_REARM, "wwawd", Items.REDSTONE_TORCH, EmptyAction.empty(), ReplenishRule.defaultRule(), new StratagemProperties(0, -1, 1200, -1, 0, false, false, Optional.of(new StratagemReplenish(Optional.empty(), "tnt", Optional.of(context.lookup(ModRegistries.STRATAGEM).getOrThrow(ModConstants.StratagemTag.TNT_REPLENISH)), Optional.of(SoundEvents.BEACON_ACTIVATE)))));
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