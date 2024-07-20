package com.stevekung.stratagems.registry;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.action.*;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        register(context, REINFORCE, "wsdaw", Items.RESPAWN_ANCHOR.getDescription(), new ItemStack(Items.RESPAWN_ANCHOR), ReinforceAction.reinforce(), 200, 2400);
        register(context, BOW, "ssawd", Items.BOW.getDescription(), new ItemStack(Items.BOW), SpawnItemAction.spawnItems(new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64)), 100, 6000);
        register(context, SUPPLY_CHEST, "sswd", Items.CHEST.getDescription(), new ItemStack(Items.CHEST), SpawnSupplyAction.spawnSupply(BuiltInLootTables.SPAWN_BONUS_CHEST), 200, 6000);
        register(context, IRON_SWORD, "saswd", Items.IRON_SWORD.getDescription(), new ItemStack(Items.IRON_SWORD), SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SWORD)), 100, 6000);
        register(context, IRON_PICKAXE, "saswwd", Items.IRON_PICKAXE.getDescription(), new ItemStack(Items.IRON_PICKAXE), SpawnItemAction.spawnItem(new ItemStack(Items.IRON_PICKAXE)), 200, 6000);
        register(context, BLOCK, "wdsd", Items.STONE.getDescription(), new ItemStack(Items.STONE), SpawnItemAction.spawnItem(new ItemStack(Items.STONE, 64)), 100, 1200);
        register(context, TNT, "swaswdsw", Items.TNT.getDescription(), new ItemStack(Items.TNT), SpawnBombAction.spawnBomb(40), 400, 12000);
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, Component name, ItemStack icon, StratagemAction.Builder action, int incomingDuration, int nextUseCooldown)
    {
        context.register(key, new Stratagem(code, name, Either.left(icon), action.build(), incomingDuration, Optional.empty(), nextUseCooldown));
    }

    private static ResourceKey<Stratagem> createKey(String name)
    {
        return ResourceKey.create(ModRegistries.STRATAGEM, StratagemsMod.id(name));
    }
}