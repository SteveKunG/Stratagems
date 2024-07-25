package com.stevekung.stratagems.registry;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemProperties;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.action.*;
import com.stevekung.stratagems.rule.DefaultStratagemRule;
import com.stevekung.stratagems.rule.ReinforceStratagemRule;
import com.stevekung.stratagems.rule.StratagemRule;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

    private static final int BLUE_COLOR = FastColor.ARGB32.color(115, 215, 255);
    private static final int RED_COLOR = FastColor.ARGB32.color(255, 60, 60);

    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        register(context, REINFORCE, "wsdaw", new ItemStack(Items.RESPAWN_ANCHOR), ReinforceAction.reinforce(), ReinforceStratagemRule.defaultRule(), 0, 2400, 20, BLUE_COLOR, Optional.empty());
        register(context, BOW, "ssawd", new ItemStack(Items.BOW), SpawnItemAction.spawnItems(new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64)), 100, 6000, BLUE_COLOR);
        register(context, SUPPLY_CHEST, "sswd", new ItemStack(Items.CHEST), SpawnSupplyAction.spawnSupply(BuiltInLootTables.SPAWN_BONUS_CHEST), 200, 6000, BLUE_COLOR);
        register(context, IRON_SWORD, "saswd", new ItemStack(Items.IRON_SWORD), SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SWORD)), 100, 6000, BLUE_COLOR);
        register(context, IRON_PICKAXE, "saswwd", new ItemStack(Items.IRON_PICKAXE), SpawnItemAction.spawnItem(new ItemStack(Items.IRON_PICKAXE)), 200, 6000, BLUE_COLOR);
        register(context, BLOCK, "wdsd", new ItemStack(Items.STONE), SpawnItemAction.spawnItem(new ItemStack(Items.STONE, 64)), 100, 1200, BLUE_COLOR);
        register(context, TNT, "swaswdsw", new ItemStack(Items.TNT), SpawnBombAction.spawnBomb(40), DefaultStratagemRule.defaultRule(), 400, 12000, 3, RED_COLOR, Optional.of(true));
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemStack icon, StratagemAction.Builder action, StratagemRule.Builder rule, int incomingDuration, int cooldown, int remainingUse, int beamColor, Optional<Boolean> canDepleted)
    {
        context.register(key, new Stratagem(code, Component.translatable(key.location().toLanguageKey("stratagem")), Either.left(icon), action.build(), rule.build(), new StratagemProperties(incomingDuration, Optional.empty(), cooldown, Optional.of(remainingUse), beamColor, canDepleted)));
    }

    static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemStack icon, StratagemAction.Builder action, int incomingDuration, int cooldown, int beamColor)
    {
        context.register(key, new Stratagem(code, Component.translatable(key.location().toLanguageKey("stratagem")), Either.left(icon), action.build(), DefaultStratagemRule.defaultRule().build(), new StratagemProperties(incomingDuration, Optional.empty(), cooldown, Optional.empty(), beamColor, Optional.empty())));
    }

    private static ResourceKey<Stratagem> createKey(String name)
    {
        return ResourceKey.create(ModRegistries.STRATAGEM, StratagemsMod.id(name));
    }
}