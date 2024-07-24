package com.stevekung.stratagems.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.*;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class StratagemCommands
{
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.add.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_CONTAINS_DEFAULT_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.clear.contains_default.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.clear.specific.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        //@formatter:off
        dispatcher.register(Commands.literal("stratagem")
                .executes(commandContext -> test(commandContext.getSource()))
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("clear")
                        .executes(commandContext -> clearAllStratagem(commandContext.getSource()))
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> clearAllStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM)))))
                .then(Commands.literal("add")
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> addStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))));
        //@formatter:on
    }

    private static int test(CommandSourceStack source)
    {
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server.overworld()).getStratagemData();
        var stratagem = Stratagems.REINFORCE;
//        var stratagem = Stratagems.BLOCK;
//        var stratagem = Stratagems.SUPPLY_CHEST;
        System.out.println("Test use " + stratagem.location());
        stratagemData.useStratagem(stratagem);
        return 1;
    }

    private static int addStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server).getStratagemData();
        var stratagem = stratagemHolder.value();
        var i = 0;

        var compoundTag = new CompoundTag();
        compoundTag.putString(ModConstants.Tag.STRATAGEM, stratagemHolder.getRegisteredName());
        stratagemData.getStratagemList().add(new StratagemsTicker(source.getLevel(), compoundTag));

        if (i == 0)
        {
            throw ERROR_ADD_FAILED.create();
        }
        else
        {
//            if (targets.size() == 1)
//            {
//                source.sendSuccess(() -> Component.translatable("commands.effect.give.success.single", stratagem.getDisplayName(), targets.iterator().next().getDisplayName(), j / 20), true);
//            }
//            else
//            {
//                source.sendSuccess(() -> Component.translatable("commands.effect.give.success.multiple", stratagem.getDisplayName(), targets.size(), j / 20), true);
//            }

            return i;
        }
    }

    private static int clearAllStratagem(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server).getStratagemData();

        if (stratagemData.getStratagemList().stream().map(ticker -> ticker.getStratagem().unwrapKey().get()).allMatch(StratagemsData.DEFAULT_STRATAGEMS::contains))
        {
            throw ERROR_CLEAR_CONTAINS_DEFAULT_FAILED.create();
        }

        stratagemData.getStratagemList().clear();
        stratagemData.addDefaultStratagems();
        source.sendSuccess(() -> Component.literal("commands.stratagem.clear.everything.success"), true);
        return 1;
    }

    private static int clearAllStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server).getStratagemData();

        if (StratagemsData.DEFAULT_STRATAGEMS.contains(stratagemHolder.unwrapKey().get()))
        {
            throw ERROR_CLEAR_CONTAINS_DEFAULT_FAILED.create();
        }

        if (stratagemData.getStratagemList().stream().noneMatch(ticker -> ticker.getStratagem() == stratagemHolder))
        {
            throw ERROR_CLEAR_SPECIFIC_FAILED.create();
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.clear.specific.success.single", stratagem.name()), true);
            return 1;
        }
    }
}