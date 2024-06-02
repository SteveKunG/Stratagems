package com.stevekung.stratagems;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Stratagems
{
    public static final Stratagem BOW = register("bow", new Stratagem("ssawd", "Bow", new ItemStack(Items.BOW), 0, 1200, 6000));
    public static final Stratagem CHEST = register("chest", new Stratagem("sswd", "Chest", new ItemStack(Items.CHEST), 0, 200, 6000));
    public static final Stratagem SWORD = register("sword", new Stratagem("saswd", "Sword", new ItemStack(Items.IRON_SWORD), 0, 1200, 2400));
    public static final Stratagem BLOCK = register("block", new Stratagem("wdsd", "Block", new ItemStack(Items.STONE), 0, 1200, 6000));
    public static final Stratagem PICKAXE = register("pickaxe", new Stratagem("saswwd", "Pickaxe", new ItemStack(Items.IRON_PICKAXE), 0, 1200, 6000));
    public static final Stratagem BED = register("bed", new Stratagem("swaswdsw", "Bed", new ItemStack(Items.WHITE_BED), 0, 1200, 6000));

    public static void init()
    {
        StratagemsMod.LOGGER.info("Registering stratagems");
    }

    private static Stratagem register(String name, Stratagem stratagem)
    {
        return Registry.register(StratagemsMod.STRATAGEM_REGISTRY, StratagemsMod.id(name), stratagem);
    }
}