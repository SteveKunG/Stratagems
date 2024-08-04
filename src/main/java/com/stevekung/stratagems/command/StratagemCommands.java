package com.stevekung.stratagems.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.packet.UpdatePlayerStratagemsPacket;
import com.stevekung.stratagems.packet.UpdateServerStratagemsPacket;
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
        var serverStratagems = server.overworld().getStratagemData();
        serverStratagems.getInstances().forEach(instance -> instance.reset(server, source.getPlayer()));
        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getStratagems().values().forEach(instance -> instance.reset(server, serverPlayer)));
        sendAllPacket(source);
        sendAllPlayerStratagemPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.everything.success"), true);
        return 1;
    }

    private static int resetServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        //stratagem reset server stratagems:reinforce
        //stratagem reset server stratagems:block
        //stratagem reset server stratagems:tnt
        //stratagem reset server stratagems:tnt_rearm
        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatchHolder(serverStratagems.getInstances(), holder))
        {
            throw ERROR_RESET_SERVER_SPECIFIC_FAILED.create();
        }

        serverStratagems.reset(holder);
        sendServerStratagemPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        return 1;
    }

    private static int resetPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        //stratagem reset
        var playerStratagems = serverPlayer.getStratagems();

        if (StratagemUtils.noneMatchHolder(playerStratagems.values(), holder))
        {
            throw ERROR_RESET_PLAYER_SPECIFIC_FAILED.create();
        }

        playerStratagems.get(holder).reset(source.getServer(), serverPlayer);
        sendPlayerStratagemPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int listPlayerStratagems(CommandSourceStack source, ServerPlayer serverPlayer) throws CommandSyntaxException
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
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.server", instances.size(), StratagemUtils.decorateStratagemList(instances)), true);
            return instances.size();
        }
    }

    private static int useServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        //stratagem use server stratagems:reinforce
        //stratagem use server stratagems:block
        //stratagem use server stratagems:tnt
        //stratagem use server stratagems:tnt_rearm

        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatchHolder(serverStratagems.getInstances(), holder))
        {
            throw ERROR_USE_SERVER_SPECIFIC_FAILED.create();
        }

        serverStratagems.use(holder, source.getPlayer());
        sendServerStratagemPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        return 1;
    }

    private static int usePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        //stratagem use player stratagems:reinforce
        //stratagem use player stratagems:block
        //stratagem use player stratagems:tnt
        //stratagem use player stratagems:tnt_rearm

        var playerStratagems = serverPlayer.getStratagems();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatchHolder(playerStratagems.values(), holder))
        {
            throw ERROR_USE_PLAYER_SPECIFIC_FAILED.create();
        }

        playerStratagems.get(holder).use(source.getServer(), serverPlayer);
        sendPlayerStratagemPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int addServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        //stratagem add server stratagems:block
        //stratagem add server stratagems:bow
        //stratagem add server stratagems:tnt
        //stratagem add server stratagems:tnt_rearm

        var server = source.getServer();
        var serverStratagems = server.overworld().getStratagemData();
        var stratagem = holder.value();

        if (StratagemUtils.anyMatchHolder(serverStratagems.getInstances(), holder))
        {
            throw ERROR_ADD_SERVER_FAILED.create();
        }
        else
        {
            serverStratagems.add(StratagemUtils.createInstanceWithDefaultValue(holder, StratagemInstance.Side.SERVER));
            sendServerStratagemPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            return 1;
        }
    }

    private static int addPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder)
    {
        //stratagem add player stratagems:block
        //stratagem add player stratagems:bow
        //stratagem add player stratagems:tnt
        //stratagem add player stratagems:tnt_rearm

        var playerStratagems = serverPlayer.getStratagems();
        var stratagem = holder.value();

        if (StratagemUtils.anyMatchHolder(playerStratagems.values(), holder))
        {
            source.sendFailure(Component.translatable("commands.stratagem.add.player.failed", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            playerStratagems.put(holder, StratagemUtils.createInstanceWithDefaultValue(holder, StratagemInstance.Side.PLAYER));
            sendPlayerStratagemPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.player.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder), serverPlayer.getDisplayName()), true);
            return 1;
        }
    }

    private static int removeAllStratagem(CommandSourceStack source)
    {
        var server = source.getServer();
        server.overworld().getStratagemData().clear();
        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getStratagems().clear());
        sendAllPacket(source);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.everything.success"), true);
        return 1;
    }

    private static int removeServerStratagem(CommandSourceStack source, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagem = holder.value();
        var serverStratagems = server.overworld().getStratagemData();

        if (StratagemUtils.noneMatchHolder(serverStratagems.getInstances(), holder))
        {
            throw ERROR_REMOVE_SERVER_SPECIFIC_FAILED.create();
        }
        else
        {
            serverStratagems.remove(holder);
            sendServerStratagemPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.specific.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            return 1;
        }
    }

    private static int removePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> holder) throws CommandSyntaxException
    {
        var stratagem = holder.value();
        var playerStratagems = serverPlayer.getStratagems();

        if (StratagemUtils.noneMatchHolder(playerStratagems.values(), holder))
        {
            throw ERROR_REMOVE_PLAYER_SPECIFIC_FAILED.create();
        }
        else
        {
            playerStratagems.remove(holder);
            sendPlayerStratagemPacket(source);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.player.specific.success", serverPlayer.getDisplayName(), StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
            return 1;
        }
    }

    private static void sendServerStratagemPacket(CommandSourceStack source)
    {
        for (var player : PlayerLookup.all(source.getServer()))
        {
            ServerPlayNetworking.send(player, UpdateServerStratagemsPacket.create(player.serverLevel().getStratagemData().getInstances()));
        }
    }

    private static void sendPlayerStratagemPacket(CommandSourceStack source)
    {
        var player = source.getPlayer();
        ServerPlayNetworking.send(player, UpdatePlayerStratagemsPacket.create(player.getStratagems().values(), player.getUUID()));
    }

    private static void sendAllPlayerStratagemPacket(CommandSourceStack source)
    {
        for (var player : PlayerLookup.all(source.getServer()))
        {
            ServerPlayNetworking.send(player, UpdatePlayerStratagemsPacket.create(player.getStratagems().values(), player.getUUID()));
        }
    }

    private static void sendAllPacket(CommandSourceStack source)
    {
        sendPlayerStratagemPacket(source);
        sendServerStratagemPacket(source);
    }
}