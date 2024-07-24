package com.stevekung.stratagems.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.*;
import com.stevekung.stratagems.registry.ModRegistries;
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
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("use")
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> testUseStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM)))))
                .then(Commands.literal("clear")
                        .executes(commandContext -> clearAllStratagem(commandContext.getSource()))
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> clearAllStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM)))))
                .then(Commands.literal("add")
                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                .executes(commandContext -> addStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))));
        //@formatter:on
    }

    private static int testUseStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem use stratagems:reinforce
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server.overworld()).getStratagemData();
        stratagemData.useStratagem(stratagemHolder.unwrapKey().get());
        return 1;
    }

    private static int addStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem add stratagems:block
        //stratagem add stratagems:bow
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server.overworld()).getStratagemData();
        var stratagem = stratagemHolder.value();
        var properties = stratagem.properties();

        if (stratagemData.getStratagemList().stream().map(StratagemsTicker::getStratagem).anyMatch(holder -> holder == stratagemHolder))
        {
            throw ERROR_ADD_FAILED.create();
        }
        else
        {
            var compoundTag = new CompoundTag();
            compoundTag.putString(ModConstants.Tag.STRATAGEM, stratagemHolder.unwrapKey().get().location().toString());
            compoundTag.putInt(ModConstants.Tag.INCOMING_DURATION, properties.incomingDuration());
            compoundTag.putInt(ModConstants.Tag.DURATION, properties.duration().orElse(0));
            compoundTag.putInt(ModConstants.Tag.NEXT_USE_COOLDOWN, properties.nextUseCooldown());
            compoundTag.putInt(ModConstants.Tag.REMAINING_USE, properties.remainingUse().orElse(-1));
            compoundTag.putString(ModConstants.Tag.STATE, StratagemsTicker.State.READY.getName());
            stratagemData.getStratagemList().add(new StratagemsTicker(source.getLevel(), compoundTag));
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.success", stratagem.name()), true);
        }
        return 1;
    }

    private static int clearAllStratagem(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server.overworld()).getStratagemData();

        if (stratagemData.getStratagemList().stream().map(ticker -> ticker.getStratagem().unwrapKey().get()).allMatch(StratagemsData.DEFAULT_STRATAGEMS::contains))
        {
            throw ERROR_CLEAR_CONTAINS_DEFAULT_FAILED.create();
        }

        stratagemData.getStratagemList().clear();
        stratagemData.addDefaultStratagems(server.registryAccess());
        source.sendSuccess(() -> Component.literal("commands.stratagem.clear.everything.success"), true);
        return 1;
    }

    private static int clearAllStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var server = source.getServer();
        var stratagemData = ((StratagemsDataAccessor) server.overworld()).getStratagemData();

        if (StratagemsData.DEFAULT_STRATAGEMS.contains(stratagemHolder.unwrapKey().get()))
        {
            throw ERROR_CLEAR_CONTAINS_DEFAULT_FAILED.create();
        }

        if (stratagemData.getStratagemList().stream().map(StratagemsTicker::getStratagem).noneMatch(holder -> holder == stratagemHolder))
        {
            throw ERROR_CLEAR_SPECIFIC_FAILED.create();
        }
        else
        {
            stratagemData.getStratagemList().removeIf(ticker -> ticker.getStratagem() == stratagemHolder);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.clear.specific.success", stratagem.name()), true);
            return 1;
        }
    }
}