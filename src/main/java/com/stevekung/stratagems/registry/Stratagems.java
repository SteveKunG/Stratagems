package com.stevekung.stratagems.registry;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemsMod;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Stratagems
{
    public static final ResourceKey<Stratagem> BOW = createKey("bow");
    public static final ResourceKey<Stratagem> SUPPLY_CHEST = createKey("supply_chest");
    public static final ResourceKey<Stratagem> IRON_SWORD = createKey("iron_sword");
    public static final ResourceKey<Stratagem> IRON_PICKAXE = createKey("iron_pickaxe");
    public static final ResourceKey<Stratagem> BLOCK = createKey("block");
    public static final ResourceKey<Stratagem> BED = createKey("bed");

    public static void bootstrap(BootstapContext<Stratagem> context)
    {
        register(context, BOW, "ssawd", Items.BOW.getDescription(), new ItemStack(Items.BOW), 100, 6000);
        register(context, SUPPLY_CHEST, "sswd", Items.CHEST.getDescription(), new ItemStack(Items.CHEST), 200, 6000);
        register(context, IRON_SWORD, "saswd", Items.IRON_SWORD.getDescription(), new ItemStack(Items.IRON_SWORD), 100, 6000);
        register(context, IRON_PICKAXE, "saswwd", Items.IRON_PICKAXE.getDescription(), new ItemStack(Items.IRON_PICKAXE), 200, 6000);
        register(context, BLOCK, "wdsd", Items.STONE.getDescription(), new ItemStack(Items.STONE), 100, 1200);
        register(context, BED, "swaswdsw", Items.WHITE_BED.getDescription(), new ItemStack(Items.WHITE_BED), 400, 12000);
    }

    static void register(BootstapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, Component name, ItemStack icon, int incomingDuration, int nextUseCooldown)
    {
        context.register(key, new Stratagem(code, name, Either.left(icon), incomingDuration, Optional.empty(), nextUseCooldown));
    }

    private static ResourceKey<Stratagem> createKey(String name)
    {
        return ResourceKey.create(StratagemsMod.STRATAGEM_KEY, StratagemsMod.id(name));
    }
}