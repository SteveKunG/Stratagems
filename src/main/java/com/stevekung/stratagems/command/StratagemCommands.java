package com.stevekung.stratagems.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemUtils;
import com.stevekung.stratagems.StratagemsData;
import com.stevekung.stratagems.StratagemEntry;
import com.stevekung.stratagems.registry.ModRegistries;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

public class StratagemCommands
{
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_CONTAINS_DEFAULT_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.contains_default.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.specific.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        //@formatter:off
        dispatcher.register(Commands.literal("stratagem")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("use")
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> useStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM)))))
                .then(Commands.literal("add")
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> addStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM)))))
                .then(Commands.literal("remove")
                        .executes(commandContext -> removeAllStratagem(commandContext.getSource()))
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> removeStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM)))))
                .then(Commands.literal("list")
                        .executes(commandContext -> listStratagem(commandContext.getSource())))
                .then(Commands.literal("reset")
                        .executes(commandContext -> resetStratagem(commandContext.getSource()))));
        //@formatter:on
    }

    private static int resetStratagem(CommandSourceStack source)
    {
        //stratagem reset
        var server = source.getServer();
        var stratagemData = server.overworld().getStratagemData();
        stratagemData.reset();
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset"), true);
        return 1;
    }

    private static int listStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        var stratagemData = server.overworld().getStratagemData();
        source.sendSuccess(() -> Component.translatable("commands.stratagem.list", stratagemData.getStratagemEntries().stream().map(entry -> entry.getResourceKey().location()).toList()), true);
        return 1;
    }

    private static int useStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder)
    {
        //stratagem use stratagems:reinforce
        //stratagem use stratagems:block
        //stratagem use stratagems:tnt
        var server = source.getServer();
        var stratagemData = server.overworld().getStratagemData();
        stratagemData.use(stratagemHolder.unwrapKey().orElseThrow());
        return 1;
    }

    private static int addStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem add stratagems:block
        //stratagem add stratagems:bow
        //stratagem add stratagems:tnt
        var server = source.getServer();
        var stratagemData = server.overworld().getStratagemData();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.anyMatchHolder(stratagemData.getStratagemEntries(), stratagemHolder))
        {
            throw ERROR_ADD_FAILED.create();
        }
        else
        {
            stratagemData.add(StratagemUtils.createCompoundTagWithDefaultValue(stratagemHolder));
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.success", stratagem.name()), true);
            return 1;
        }
    }

    private static int removeAllStratagem(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemData = server.overworld().getStratagemData();

        if (stratagemData.getStratagemEntries().stream().map(StratagemEntry::getResourceKey).allMatch(StratagemsData.DEFAULT_STRATAGEMS::contains))
        {
            throw ERROR_REMOVE_CONTAINS_DEFAULT_FAILED.create();
        }

        stratagemData.clear();
        source.sendSuccess(() -> Component.literal("commands.stratagem.remove.everything.success"), true);
        return 1;
    }

    private static int removeStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var server = source.getServer();
        var stratagemData = server.overworld().getStratagemData();

        if (StratagemsData.DEFAULT_STRATAGEMS.contains(stratagemHolder.unwrapKey().orElseThrow()))
        {
            throw ERROR_REMOVE_CONTAINS_DEFAULT_FAILED.create();
        }

        if (StratagemUtils.noneMatchHolder(stratagemData.getStratagemEntries(), stratagemHolder))
        {
            throw ERROR_REMOVE_SPECIFIC_FAILED.create();
        }
        else
        {
            stratagemData.remove(stratagemHolder);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.specific.success", stratagem.name()), true);
            return 1;
        }
    }
}