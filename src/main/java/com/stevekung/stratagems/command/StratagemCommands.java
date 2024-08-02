package com.stevekung.stratagems.command;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.packet.UpdateStratagemsPacket;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.util.StratagemUtils;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StratagemCommands
{
    private static final SimpleCommandExceptionType ERROR_ADD_SERVER_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.add.server.failed"));
    private static final SimpleCommandExceptionType ERROR_USE_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.use.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_USE_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.use.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_RESET_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.reset.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_RESET_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.reset.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_LIST_EMPTY_SERVER = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.list.server.empty"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        // stratagem add player @s stratagem
        // stratagem use player @s stratagem
        // stratagem remove player @s stratagem
        // stratagem reset player @s stratagem
        // stratagem list player @s

        // stratagem add server stratagem
        // stratagem use server stratagem
        // stratagem remove server stratagem
        // stratagem reset server stratagem
        // stratagem list server

        //@formatter:off
        dispatcher.register(Commands.literal("stratagem")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))

                .then(Commands.literal("add")
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> addPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> addServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))

                .then(Commands.literal("use")
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> usePlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> useServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))

                .then(Commands.literal("remove")
                        .then(Commands.literal("*")
                                .executes(commandContext -> removeAllStratagem(commandContext.getSource())))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> removePlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> removeServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))

                .then(Commands.literal("reset")
                        .then(Commands.literal("*")
                                .executes(commandContext -> resetAllStratagem(commandContext.getSource())))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> resetPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> resetServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))

                .then(Commands.literal("list")
                        .then(Commands.literal("player")
                                .executes(commandContext -> listPlayerStratagems(commandContext.getSource(), commandContext.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.players())
                                        .executes(commandContext -> listPlayerStratagems(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player")))))
                        .then(Commands.literal("server")
                                .executes(commandContext -> listServerStratagems(commandContext.getSource()))))
                );
        //@formatter:on
    }

    private static int resetAllStratagem(CommandSourceStack source) throws CommandSyntaxException
    {
        //stratagem reset
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        stratagemData.getStratagemInstances().forEach(instance -> instance.reset(server, source.getPlayer()));
        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getPlayerStratagems().values().forEach(instance -> instance.reset(server, serverPlayer)));
        sendPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.everything.success"), true);
        return 1;
    }

    private static int resetServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem reset server stratagems:reinforce
        //stratagem reset server stratagems:block
        //stratagem reset server stratagems:tnt
        //stratagem reset server stratagems:tnt_rearm
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.noneMatchHolder(stratagemData.getStratagemInstances(), stratagemHolder))
        {
            throw ERROR_RESET_SERVER_SPECIFIC_FAILED.create();
        }

        stratagemData.reset(stratagemHolder);
        sendPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder)), true);
        return 1;
    }

    private static int resetPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem reset
        var stratagemData = serverPlayer.getPlayerStratagems().get(stratagemHolder);

        if (StratagemUtils.noneMatchHolder(List.copyOf(serverPlayer.getPlayerStratagems().values()), stratagemHolder))
        {
            throw ERROR_RESET_PLAYER_SPECIFIC_FAILED.create();
        }

        stratagemData.reset(source.getServer(), serverPlayer);
        sendPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int listPlayerStratagems(CommandSourceStack source, ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var stratagemData = serverPlayer.getPlayerStratagems();

        if (stratagemData.isEmpty())
        {
            source.sendFailure(Component.translatable("commands.stratagem.list.player.empty", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.player", serverPlayer.getDisplayName(), stratagemData.size(), StratagemUtils.decorateStratagemList(List.copyOf(stratagemData.values()))), true);
            return stratagemData.size();
        }
    }

    private static int listServerStratagems(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        var stratagemInstances = stratagemData.getStratagemInstances();

        if (stratagemInstances.isEmpty())
        {
            throw ERROR_LIST_EMPTY_SERVER.create();
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.server", stratagemInstances.size(), StratagemUtils.decorateStratagemList(stratagemInstances)), true);
            return stratagemInstances.size();
        }
    }

    private static int useServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem use server stratagems:reinforce
        //stratagem use server stratagems:block
        //stratagem use server stratagems:tnt
        //stratagem use server stratagems:tnt_rearm

        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.noneMatchHolder(stratagemData.getStratagemInstances(), stratagemHolder))
        {
            throw ERROR_USE_SERVER_SPECIFIC_FAILED.create();
        }

        stratagemData.use(stratagemHolder.unwrapKey().orElseThrow(), source.getPlayer());
        sendPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder)), true);
        return 1;
    }

    private static int usePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem use player stratagems:reinforce
        //stratagem use player stratagems:block
        //stratagem use player stratagems:tnt
        //stratagem use player stratagems:tnt_rearm

        var stratagemInstance = serverPlayer.getPlayerStratagems().get(stratagemHolder);
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.noneMatchHolder(List.copyOf(serverPlayer.getPlayerStratagems().values()), stratagemHolder))
        {
            throw ERROR_USE_PLAYER_SPECIFIC_FAILED.create();
        }

        stratagemInstance.use(source.getServer(), serverPlayer);
        sendPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder), serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int addServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem add server stratagems:block
        //stratagem add server stratagems:bow
        //stratagem add server stratagems:tnt
        //stratagem add server stratagems:tnt_rearm

        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.anyMatchHolder(stratagemData.getStratagemInstances(), stratagemHolder))
        {
            throw ERROR_ADD_SERVER_FAILED.create();
        }
        else
        {
            stratagemData.add(StratagemUtils.createInstanceWithDefaultValue(stratagemHolder, StratagemInstance.Side.SERVER));
            sendPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder)), true);
            return 1;
        }
    }

    private static int addPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder)
    {
        //stratagem add player stratagems:block
        //stratagem add player stratagems:bow
        //stratagem add player stratagems:tnt
        //stratagem add player stratagems:tnt_rearm

        var stratagemData = serverPlayer.getPlayerStratagems();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.anyMatchHolder(List.copyOf(stratagemData.values()), stratagemHolder))
        {
            source.sendFailure(Component.translatable("commands.stratagem.add.player.failed", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            stratagemData.put(stratagemHolder, StratagemUtils.createInstanceWithDefaultValue(stratagemHolder, StratagemInstance.Side.PLAYER));
            sendPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder), serverPlayer.getDisplayName()), true);
            return 1;
        }
    }

    private static int removeAllStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        stratagemData.clear();
        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getPlayerStratagems().clear());
        sendPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.everything.success"), true);
        return 1;
    }

    private static int removeServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();

        if (StratagemUtils.noneMatchHolder(stratagemData.getStratagemInstances(), stratagemHolder))
        {
            throw ERROR_REMOVE_SERVER_SPECIFIC_FAILED.create();
        }
        else
        {
            stratagemData.remove(stratagemHolder);
            sendPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.specific.success", StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder)), true);
            return 1;
        }
    }

    private static int removePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var stratagemData = serverPlayer.getPlayerStratagems();

        if (StratagemUtils.noneMatchHolder(List.copyOf(stratagemData.values()), stratagemHolder))
        {
            throw ERROR_REMOVE_PLAYER_SPECIFIC_FAILED.create();
        }
        else
        {
            stratagemData.remove(stratagemHolder);
            sendPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.player.specific.success", serverPlayer.getDisplayName(), StratagemUtils.decorateStratagemName(stratagem.name(), stratagemHolder)), true);
            return 1;
        }
    }

    private static void sendPacket(CommandSourceStack source)
    {
        for (var player : PlayerLookup.all(source.getServer()))
        {
            ServerPlayNetworking.send(player, UpdateStratagemsPacket.create(player.serverLevel().getServerStratagemData().getStratagemInstances(), List.copyOf(player.getPlayerStratagems().values()), player.getUUID()));
        }
    }
}