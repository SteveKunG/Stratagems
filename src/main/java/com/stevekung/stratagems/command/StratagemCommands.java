package com.stevekung.stratagems.command;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.packet.ClearStratagemsPacket;
import com.stevekung.stratagems.api.packet.StratagemEntryData;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
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
                                                .executes(commandContext -> addStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> addStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null)))))

                .then(Commands.literal("remove")
                        .then(Commands.literal("*")
                                .executes(commandContext -> removeAllStratagems(commandContext.getSource())))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> removeStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"))))
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> removeAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> removeStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null)))
                                .then(Commands.literal("*")
                                        .executes(commandContext -> removeAllServerStratagem(commandContext.getSource())))))

                .then(Commands.literal("use")
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> useStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> useStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null)))))


                .then(Commands.literal("reset")
                        .then(Commands.literal("*")
                                .executes(commandContext -> resetAllStratagem(commandContext.getSource())))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> resetStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"))))
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> resetAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> resetStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null)))
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

    private static int addStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.stratagemsData();
        var stratagem = holder.value();

        if (StratagemUtils.anyMatch(stratagemsData, holder))
        {
            if (isPlayer)
            {
                source.sendFailure(Component.translatable("commands.stratagem.add.player.failed", serverPlayer.getDisplayName()));
                return 0;
            }
            else
            {
                throw ERROR_ADD_SERVER_FAILED.create();
            }
        }
        else
        {
            stratagemsData.add(holder);

            if (isPlayer)
            {
                source.sendSuccess(() -> Component.translatable("commands.stratagem.add.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
            }
            else
            {
                source.sendSuccess(() -> Component.translatable("commands.stratagem.add.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            }
            sendStratagemPacket(source, serverPlayer, UpdateStratagemPacket.Action.ADD, stratagemsData.instanceByHolder(holder));
            return 1;
        }
    }

    private static int removeAllStratagems(CommandSourceStack source)
    {
        var server = source.getServer();
        server.stratagemsData().clear();

        for (var player : server.getPlayerList().getPlayers())
        {
            player.stratagemsData().clear();
            player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(true, true, player.getUUID())));
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.everything.success"), true);
        return 1;
    }

    private static int removeStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagem = holder.value();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.stratagemsData();

        if (StratagemUtils.noneMatch(stratagemsData, holder))
        {
            if (isPlayer)
            {
                source.sendFailure(Component.translatable("commands.stratagem.remove.player.specific.failed", serverPlayer.getDisplayName()));
                return 0;
            }
            else
            {
                throw ERROR_REMOVE_SERVER_SPECIFIC_FAILED.create();
            }
        }
        else
        {
            // send a remove packet to players before remove from player!
            sendStratagemPacket(source, serverPlayer, UpdateStratagemPacket.Action.REMOVE, stratagemsData.instanceByHolder(holder));
            stratagemsData.remove(holder);

            if (isPlayer)
            {
                source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.player.specific.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
            }
            else
            {
                source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.specific.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            }
            return 1;
        }
    }

    private static int removeAllServerStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        server.stratagemsData().clear();

        for (var player : source.getServer().getPlayerList().getPlayers())
        {
            player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(true)));
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.everything.success"), true);
        return 1;
    }

    private static int removeAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        serverPlayer.stratagemsData().clear();

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
        var serverStratagems = server.stratagemsData();

        serverStratagems.reset();

        for (var entry : serverStratagems.instances().entrySet())
        {
            updateServerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, entry.getValue());
        }

        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.stratagemsData().instances().values().forEach(instance ->
        {
            instance.reset(server, serverPlayer);
            updatePlayerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, instance);
        }));

        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.everything.success"), true);
        return 1;
    }

    private static int resetStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemsData = server.stratagemsData();
        var isPlayer = serverPlayer != null;
        var stratagem = holder.value();

        if (StratagemUtils.noneMatch(stratagemsData, holder))
        {
            if (isPlayer)
            {
                throw ERROR_RESET_PLAYER_SPECIFIC_FAILED.create();
            }
            else
            {
                throw ERROR_RESET_SERVER_SPECIFIC_FAILED.create();
            }
        }

        stratagemsData.reset(holder);
        sendStratagemPacket(source, serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));

        if (isPlayer)
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.success", serverPlayer.getDisplayName()), true);
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        }
        return 1;
    }

    private static int resetAllServerStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        var serverStratagems = server.stratagemsData();
        var instances = serverStratagems.instances();

        serverStratagems.reset();

        for (var entry : instances.entrySet())
        {
            updateServerStratagemPacket(source, UpdateStratagemPacket.Action.UPDATE, entry.getValue());
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.everything.success"), true);
        return 1;
    }

    private static int resetAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        for (var entry : serverPlayer.stratagemsData().instances().entrySet())
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
        var playerStratagems = serverPlayer.stratagemsData();

        if (playerStratagems.instances().isEmpty())
        {
            source.sendFailure(Component.translatable("commands.stratagem.list.player.empty", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.player", serverPlayer.getDisplayName(), playerStratagems.instances().size(), StratagemUtils.decorateStratagemList(playerStratagems.instances().values())), true);
            return playerStratagems.instances().size();
        }
    }

    private static int listServerStratagems(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var instances = server.stratagemsData().instances();

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

    private static int useStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.stratagemsData();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatch(stratagemsData, holder))
        {
            if (isPlayer)
            {
                throw ERROR_USE_PLAYER_SPECIFIC_FAILED.create();
            }
            else
            {
                throw ERROR_USE_SERVER_SPECIFIC_FAILED.create();
            }
        }

        stratagemsData.use(holder, source.getPlayer());
        sendStratagemPacket(source, serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));

        if (isPlayer)
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.use.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.use.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        }
        return 1;
    }

    private static void sendStratagemPacket(CommandSourceStack source, ServerPlayer serverPlayer, UpdateStratagemPacket.Action action, StratagemInstance instance)
    {
        if (serverPlayer != null)
        {
            serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(action, StratagemEntryData.fromInstance(instance), serverPlayer.getUUID())));
        }
        else
        {
            for (var player : source.getServer().getPlayerList().getPlayers())
            {
                player.connection.send(new ClientboundCustomPayloadPacket(new UpdateStratagemPacket(action, StratagemEntryData.fromInstance(instance))));
            }
        }
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