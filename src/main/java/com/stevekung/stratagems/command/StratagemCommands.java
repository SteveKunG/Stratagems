package com.stevekung.stratagems.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.packet.*;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;

public class StratagemCommands
{
    private static final SimpleCommandExceptionType ERROR_ADD_SERVER_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.add.server.failed"));
    private static final SimpleCommandExceptionType ERROR_USE_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.use.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_USE_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.use.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_RESET_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.reset.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_RESET_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.reset.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_LIST_EMPTY_SERVER = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.list.server.empty"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
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

                .then(Commands.literal("remove")
                        .then(Commands.literal("*")
                                .executes(commandContext -> removeAllStratagem(commandContext.getSource())))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> removePlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> removeAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> removeServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))
                                .then(Commands.literal("*")
                                        .executes(commandContext -> removeAllServerStratagem(commandContext.getSource())))))

                .then(Commands.literal("use")
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> usePlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> useServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))))


                .then(Commands.literal("reset")
                        .then(Commands.literal("*")
                                .executes(commandContext -> resetAllStratagem(commandContext.getSource())))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> resetPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> resetAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> resetServerStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM))))
                                .then(Commands.literal("*")
                                        .executes(commandContext -> resetAllServerStratagem(commandContext.getSource())))))

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

    private static int addServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var instances = serverStratagems.getInstances();
        var stratagem = holder.value();

        if (StratagemUtils.anyMatchHolder(instances.values(), holder))
        {
            throw ERROR_ADD_SERVER_FAILED.create();
        }
        else
        {
            serverStratagems.add(holder, StratagemInstance.Side.SERVER);
            updateServerStratagemPacket(source, UpdateStratagemPacket.Action.ADD, instances.get(holder));
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            return 1;
        }
    }

    private static int addPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder)
    {
        var playerStratagems = serverPlayer.getStratagems();
        var stratagem = holder.value();

        if (StratagemUtils.anyMatchHolder(playerStratagems.values(), holder))
        {
            source.sendFailure(Component.translatable("commands.stratagem.add.player.failed", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            playerStratagems.put(holder, StratagemUtils.createInstanceForPlayer(holder, serverPlayer.getUniqueStratagemId()));
            updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.ADD, playerStratagems.get(holder));
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
            return 1;
        }
    }

    private static int removeAllStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        server.overworld().getStratagemData().clear();
        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getStratagems().clear());

        for (var player : source.getServer().getPlayerList().getPlayers())
        {
            player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(true, true, player.getUUID())));
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.everything.success"), true);
        return 1;
    }

    private static int removeServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagem = holder.value();
        var serverStratagems = server.overworld().getStratagemData();
        var instances = serverStratagems.getInstances();

        if (StratagemUtils.noneMatchHolder(instances.values(), holder))
        {
            throw ERROR_REMOVE_SERVER_SPECIFIC_FAILED.create();
        }
        else
        {
            updateServerStratagemPacket(source, UpdateStratagemPacket.Action.REMOVE, instances.get(holder));
            serverStratagems.remove(holder);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.specific.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            return 1;
        }
    }

    private static int removeAllServerStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        server.overworld().getStratagemData().clear();

        for (var player : source.getServer().getPlayerList().getPlayers())
        {
            player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(true)));
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.everything.success"), true);
        return 1;
    }

    private static int removePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var stratagem = holder.value();
        var playerStratagems = serverPlayer.getStratagems();

        if (StratagemUtils.noneMatchHolder(playerStratagems.values(), holder))
        {
            source.sendFailure(Component.translatable("commands.stratagem.remove.player.specific.failed", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.REMOVE, playerStratagems.get(holder));
            playerStratagems.remove(holder);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.player.specific.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
            return 1;
        }
    }

    private static int removeAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        serverPlayer.getStratagems().clear();

        for (var player : source.getServer().getPlayerList().getPlayers())
        {
            player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(false, true, player.getUUID())));
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.player.everything.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int resetAllStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();

        serverStratagems.reset();

        for (var entry : serverStratagems.getInstances().entrySet())
        {
            updateServerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, entry.getValue());
        }

        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getStratagems().values().forEach(instance ->
        {
            instance.reset(server, serverPlayer);
            updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, instance);
        }));

        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.everything.success"), true);
        return 1;
    }

    private static int resetServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var instances = serverStratagems.getInstances();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatchHolder(instances.values(), holder))
        {
            throw ERROR_RESET_SERVER_SPECIFIC_FAILED.create();
        }

        serverStratagems.reset(holder);
        updateServerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, instances.get(holder));
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        return 1;
    }

    private static int resetAllServerStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var instances = serverStratagems.getInstances();

        serverStratagems.reset();

        for (var entry : instances.entrySet())
        {
            updateServerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, entry.getValue());
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.everything.success"), true);
        return 1;
    }

    private static int resetPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var playerStratagems = serverPlayer.getStratagems();

        if (StratagemUtils.noneMatchHolder(playerStratagems.values(), holder))
        {
            throw ERROR_RESET_PLAYER_SPECIFIC_FAILED.create();
        }

        playerStratagems.get(holder).reset(source.getServer(), serverPlayer);
        updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, playerStratagems.get(holder));
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int resetAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        for (var entry : serverPlayer.getStratagems().entrySet())
        {
            var instance = entry.getValue();
            instance.reset(source.getServer(), serverPlayer);
            updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, instance);
        }
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.everything.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int listPlayerStratagems(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        var playerStratagems = serverPlayer.getStratagems();

        if (playerStratagems.isEmpty())
        {
            source.sendFailure(Component.translatable("commands.stratagem.list.player.empty", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.player", serverPlayer.getDisplayName(), playerStratagems.size(), StratagemUtils.decorateStratagemList(playerStratagems.values())), true);
            return playerStratagems.size();
        }
    }

    private static int listServerStratagems(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var instances = server.overworld().getStratagemData().getInstances();

        if (instances.isEmpty())
        {
            throw ERROR_LIST_EMPTY_SERVER.create();
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.server", instances.size(), StratagemUtils.decorateStratagemList(instances.values())), true);
            return instances.size();
        }
    }

    private static int useServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatchHolder(serverStratagems.getInstances().values(), holder))
        {
            throw ERROR_USE_SERVER_SPECIFIC_FAILED.create();
        }

        serverStratagems.use(holder, source.getPlayer());
        updateServerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, serverStratagems.getInstances().get(holder));
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        return 1;
    }

    private static int usePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var playerStratagems = serverPlayer.getStratagems();
        var instance = playerStratagems.get(holder);
        var stratagem = holder.value();

        if (StratagemUtils.noneMatchHolder(playerStratagems.values(), holder))
        {
            throw ERROR_USE_PLAYER_SPECIFIC_FAILED.create();
        }

        instance.use(source.getServer(), serverPlayer);
        updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, instance);
        System.out.println(instance.getResourceKey());
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static void updateServerStratagemPacket(CommandSourceStack source, UpdateStratagemPacket.Action action, StratagemInstance instance)
    {
        for (var player : source.getServer().getPlayerList().getPlayers())
        {
            player.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(action, StratagemEntryData.fromInstance(instance))));
        }
    }

    private static void updatePlayerStratagemPacket(CommandSourceStack source, UpdateStratagemPacket.Action action, StratagemInstance instance)
    {
        var player = source.getPlayer();
        player.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(action, StratagemEntryData.fromInstance(instance), player.getUUID())));
    }
}