package com.stevekung.stratagems.registry;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.stevekung.stratagems.*;
import com.stevekung.stratagems.action.*;
import com.stevekung.stratagems.rule.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    public static final ResourceKey<Stratagem> TNT_REARM = createKey("tnt_rearm");

    private static final int BLUE_COLOR = FastColor.ARGB32.color(115, 215, 255);
    private static final int RED_COLOR = FastColor.ARGB32.color(255, 60, 60);

    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        register(context, REINFORCE, "wsdaw", new ItemStack(Items.RESPAWN_ANCHOR), ReinforceAction.reinforce(), ReinforceRule.defaultRule(), StratagemProperties.withDepleted(0, 2400, 20, BLUE_COLOR));
        register(context, BOW, "ssawd", new ItemStack(Items.BOW), SpawnItemAction.spawnItems(new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64)), StratagemProperties.simple(100, 6000, BLUE_COLOR));
        register(context, SUPPLY_CHEST, "sswd", new ItemStack(Items.CHEST), SpawnSupplyAction.spawnSupply(BuiltInLootTables.SPAWN_BONUS_CHEST), StratagemProperties.simple(200, 6000, BLUE_COLOR));
        register(context, IRON_SWORD, "saswd", new ItemStack(Items.IRON_SWORD), SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SWORD)), StratagemProperties.simple(100, 6000, BLUE_COLOR));
        register(context, IRON_PICKAXE, "saswwd", new ItemStack(Items.IRON_PICKAXE), SpawnItemAction.spawnItem(new ItemStack(Items.IRON_PICKAXE)), StratagemProperties.simple(200, 6000, BLUE_COLOR));
        register(context, BLOCK, "wdsd", new ItemStack(Items.STONE), SpawnItemAction.spawnItem(new ItemStack(Items.STONE, 64)), StratagemProperties.simple(100, 1200, BLUE_COLOR));
        register(context, TNT, "swaswdsw", new ItemStack(Items.TNT), SpawnBombAction.spawnBomb(40), DepletedRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 3, RED_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), Optional.empty(), Optional.empty())));
        register(context, TNT_REARM, "wwawd", new ItemStack(Items.REDSTONE_BLOCK), EmptyAction.empty(), ReplenishRule.defaultRule(), new StratagemProperties(0, Optional.empty(), 1200, Optional.empty(), 0, Optional.empty(), Optional.of(false), Optional.of(new StratagemReplenish(Optional.empty(), Optional.of(context.lookup(ModRegistries.STRATAGEM).getOrThrow(ModConstants.StratagemTag.TNT_REPLENISH)), Optional.of(SoundEvents.BEACON_ACTIVATE)))));
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemStack icon, StratagemAction.Builder action, StratagemRule.Builder rule, StratagemProperties properties)
    {
        context.register(key, new Stratagem(code, Component.translatable(key.location().toLanguageKey("stratagem")), Either.left(icon), action.build(), rule.build(), properties));
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemStack icon, StratagemAction.Builder action, StratagemProperties properties)
    {
        context.register(key, new Stratagem(code, Component.translatable(key.location().toLanguageKey("stratagem")), Either.left(icon), action.build(), DefaultRule.defaultRule().build(), properties));
    }

    private static ResourceKey<Stratagem> createKey(String name)
    {
        return ResourceKey.create(ModRegistries.STRATAGEM, StratagemsMod.id(name));
    }
}