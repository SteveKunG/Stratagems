package com.stevekung.stratagems.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.ServerStratagemsData;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.packet.UpdateServerStratagemsPacket;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.util.StratagemUtils;

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
    private static final SimpleCommandExceptionType ERROR_ADD_PLAYER_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.add.player.failed"));
    private static final SimpleCommandExceptionType ERROR_ADD_SERVER_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.add.server.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_CONTAINS_DEFAULT_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.contains_default.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.remove.specific.failed"));

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
        sendPacket(source.getPlayer());
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.everything.success"), true);
        return 1;
    }

    private static int resetServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem reset
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        stratagemData.reset(stratagemHolder);
        sendPacket(source.getPlayer());
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.success", stratagemHolder.value().name()), true);
        return 1;
    }

    private static int resetPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder)
    {
        //stratagem reset
        var stratagemData = serverPlayer.getPlayerStratagems().get(stratagemHolder);
        stratagemData.reset(source.getServer(), serverPlayer);
        sendPacket(serverPlayer);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.success", serverPlayer.getName()), true);
        return 1;
    }

    private static int listPlayerStratagems(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        var stratagemData = serverPlayer.getPlayerStratagems().values();
        source.sendSuccess(() -> Component.translatable("commands.stratagem.list.player", serverPlayer.getName(), stratagemData.stream().map(entry -> entry.getResourceKey().location()).toList().toString()), true);
        return 1;
    }

    private static int listServerStratagems(CommandSourceStack source)
    {
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        source.sendSuccess(() -> Component.translatable("commands.stratagem.list.server", stratagemData.getStratagemInstances().stream().map(entry -> entry.getResourceKey().location()).toList().toString()), true);
        return 1;
    }

    private static int useServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem use stratagems:reinforce
        //stratagem use stratagems:block
        //stratagem use stratagems:tnt
        //stratagem use stratagems:tnt_rearm
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        stratagemData.use(stratagemHolder.unwrapKey().orElseThrow(), source.getPlayer());
        sendPacket(source.getPlayer());
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.server.success", stratagemHolder.value().name()), true);
        return 1;
    }

    private static int usePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder)
    {
        //stratagem use stratagems:reinforce
        //stratagem use stratagems:block
        //stratagem use stratagems:tnt
        //stratagem use stratagems:tnt_rearm
        var stratagemData = serverPlayer.getPlayerStratagems().get(stratagemHolder);
        stratagemData.use(source.getServer(), serverPlayer);
        sendPacket(serverPlayer);
        source.sendSuccess(() -> Component.translatable("commands.stratagem.use.player.success", stratagemHolder.value().name(), serverPlayer.getName()), true);
        return 1;
    }

    private static int addServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem add stratagems:block
        //stratagem add stratagems:bow
        //stratagem add stratagems:tnt
        //stratagem add stratagems:tnt_rearm
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.anyMatchHolder(stratagemData.getStratagemInstances(), stratagemHolder))
        {
            throw ERROR_ADD_SERVER_FAILED.create();
        }
        else
        {
            stratagemData.add(StratagemUtils.createInstanceWithDefaultValue(stratagemHolder));
            sendPacket(source.getPlayer());
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.server.success", stratagem.name()), true);
            return 1;
        }
    }

    private static int addPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        //stratagem add stratagems:block
        //stratagem add stratagems:bow
        //stratagem add stratagems:tnt
        //stratagem add stratagems:tnt_rearm
        var stratagemData = serverPlayer.getPlayerStratagems();
        var stratagem = stratagemHolder.value();

        if (StratagemUtils.anyMatchHolder(stratagemData.values().stream().toList(), stratagemHolder))
        {
            throw ERROR_ADD_PLAYER_FAILED.create();
        }
        else
        {
            stratagemData.put(stratagemHolder, StratagemUtils.createInstanceWithDefaultValue(stratagemHolder));
            sendPacket(serverPlayer);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.add.player.success", serverPlayer.getName(), stratagem.name()), true);
            return 1;
        }
    }

    private static int removeAllStratagem(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();

        if (stratagemData.getStratagemInstances().stream().map(StratagemInstance::getResourceKey).allMatch(ServerStratagemsData.DEFAULT_STRATAGEMS::contains))
        {
            throw ERROR_REMOVE_CONTAINS_DEFAULT_FAILED.create();
        }

        stratagemData.clear();
        server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.getPlayerStratagems().clear());
        sendPacket(source.getPlayer());
        source.sendSuccess(() -> Component.literal("commands.stratagem.remove.everything.success"), true);
        return 1;
    }

    private static int removeServerStratagem(CommandSourceStack source, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var server = source.getServer();
        var stratagemData = server.overworld().getServerStratagemData();

        if (ServerStratagemsData.DEFAULT_STRATAGEMS.contains(stratagemHolder.unwrapKey().orElseThrow()))
        {
            throw ERROR_REMOVE_CONTAINS_DEFAULT_FAILED.create();
        }

        if (StratagemUtils.noneMatchHolder(stratagemData.getStratagemInstances(), stratagemHolder))
        {
            throw ERROR_REMOVE_SPECIFIC_FAILED.create();
        }
        else
        {
            stratagemData.remove(stratagemHolder);
            sendPacket(source.getPlayer());
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.server.specific.success", stratagem.name()), true);
            return 1;
        }
    }

    private static int removePlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, Holder<Stratagem> stratagemHolder) throws CommandSyntaxException
    {
        var stratagem = stratagemHolder.value();
        var stratagemData = serverPlayer.getPlayerStratagems();

        if (StratagemUtils.noneMatchHolder(stratagemData.values().stream().toList(), stratagemHolder))
        {
            throw ERROR_REMOVE_SPECIFIC_FAILED.create();
        }
        else
        {
            stratagemData.remove(stratagemHolder);
            sendPacket(serverPlayer);
            source.sendSuccess(() -> Component.translatable("commands.stratagem.remove.player.specific.success", serverPlayer.getName(), stratagem.name()), true);
            return 1;
        }
    }

    private static void sendPacket(ServerPlayer serverPlayer)
    {
        ServerPlayNetworking.send(serverPlayer, UpdateServerStratagemsPacket.mapInstanceToEntry(serverPlayer.serverLevel().getServerStratagemData().getStratagemInstances()));
    }
}