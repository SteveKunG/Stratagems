package com.stevekung.stratagems.command;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemModifier;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.packet.ClearStratagemsPacket;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.util.PacketUtils;
import com.stevekung.stratagems.api.util.StratagemUtils;
import com.stevekung.stratagems.command.argument.StratagemModifierArgument;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
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
    private static final SimpleCommandExceptionType ERROR_BLOCK_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.block.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_BLOCK_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.block.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_UNBLOCK_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.unblock.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_UNBLOCK_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.unblock.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_SET_MODIFIER_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.modifier.set.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_MODIFIER_PLAYER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.modifier.clear.player.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_SET_MODIFIER_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.modifier.set.server.specific.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_MODIFIER_SERVER_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.stratagem.modifier.clear.server.specific.failed"));
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
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(commandContext -> useStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), EntityArgument.getPlayer(commandContext, "player")))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(commandContext -> useStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), null))))))

                .then(Commands.literal("block")
                        .then(Commands.literal("*")
                                .executes(commandContext -> blockAllStratagems(commandContext.getSource(), false)))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> blockStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"), false)))
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> blockAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), false)))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> blockStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null, false)))
                                .then(Commands.literal("*")
                                        .executes(commandContext -> blockAllServerStratagem(commandContext.getSource(), false)))))

                .then(Commands.literal("unblock")
                        .then(Commands.literal("*")
                                .executes(commandContext -> blockAllStratagems(commandContext.getSource(), true)))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                .executes(commandContext -> blockStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"), true)))
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> blockAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), true)))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                        .executes(commandContext -> blockStratagem(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null, true)))
                                .then(Commands.literal("*")
                                        .executes(commandContext -> blockAllServerStratagem(commandContext.getSource(), true)))))

                .then(Commands.literal("modifier")
                        .then(Commands.literal("set")
                                .then(Commands.argument("modifier", StratagemModifierArgument.stratagemModifier())
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> setModifierAllStratagems(commandContext.getSource(), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), false)))
                                        .then(Commands.literal("player")
                                                .then(Commands.argument("player", EntityArgument.players())
                                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                                .executes(commandContext -> setStratagemModifier(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), false)))
                                                        .then(Commands.literal("*")
                                                                .executes(commandContext -> setModifierAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), false)))))
                                        .then(Commands.literal("server")
                                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                        .executes(commandContext -> setStratagemModifier(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null, StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), false)))
                                                .then(Commands.literal("*")
                                                        .executes(commandContext -> setModifierAllServerStratagem(commandContext.getSource(), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), false))))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("modifier", StratagemModifierArgument.stratagemModifier())
                                        .then(Commands.literal("*")
                                                .executes(commandContext -> setModifierAllStratagems(commandContext.getSource(), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), true)))
                                        .then(Commands.literal("player")
                                                .then(Commands.argument("player", EntityArgument.players())
                                                        .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                                .executes(commandContext -> setStratagemModifier(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), EntityArgument.getPlayer(commandContext, "player"), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), true)))
                                                        .then(Commands.literal("*")
                                                                .executes(commandContext -> setModifierAllPlayerStratagem(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player"), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), true)))))
                                        .then(Commands.literal("server")
                                                .then(Commands.argument("stratagem", ResourceArgument.resource(context, ModRegistries.STRATAGEM))
                                                        .executes(commandContext -> setStratagemModifier(commandContext.getSource(), ResourceArgument.getResource(commandContext, "stratagem", ModRegistries.STRATAGEM), null, StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), true)))
                                                .then(Commands.literal("*")
                                                        .executes(commandContext -> setModifierAllServerStratagem(commandContext.getSource(), StratagemModifierArgument.getStratagemModifier(commandContext, "modifier"), true)))))))

                .then(Commands.literal("reset")
                        .then(Commands.literal("*")
                                .executes(commandContext -> resetAllStratagems(commandContext.getSource())))
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
                                .executes(commandContext -> listServerStratagems(commandContext.getSource())))));
        //@formatter:on
    }

    private static int addStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.overworld().stratagemsData();
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
            PacketUtils.sendClientUpdateStratagemPacket(server, serverPlayer, UpdateStratagemPacket.Action.ADD, stratagemsData.instanceByHolder(holder));
            return 1;
        }
    }

    private static int removeAllStratagems(CommandSourceStack source)
    {
        var server = source.getServer();
        server.overworld().stratagemsData().clear();

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
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.overworld().stratagemsData();

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
            PacketUtils.sendClientUpdateStratagemPacket(server, serverPlayer, UpdateStratagemPacket.Action.REMOVE, stratagemsData.instanceByHolder(holder));
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
        server.overworld().stratagemsData().clear();

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

    private static int blockAllStratagems(CommandSourceStack source, boolean unblock)
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().stratagemsData();

        serverStratagems.block(unblock);

        for (var instance : serverStratagems.listInstances())
        {
            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.UPDATE, instance);
        }

        server.getPlayerList().getPlayers().forEach(serverPlayer ->
        {
            serverPlayer.stratagemsData().block(unblock);

            for (var instance : serverPlayer.stratagemsData().listInstances())
            {
                PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, instance);
            }
        });

        source.sendSuccess(() -> Component.translatable(unblock ? "commands.stratagem.unblock.everything.success" : "commands.stratagem.block.everything.success"), true);
        return 1;
    }

    private static int blockStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer, boolean unblock) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.overworld().stratagemsData();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatch(stratagemsData, holder))
        {
            if (isPlayer)
            {
                if (unblock)
                {
                    throw ERROR_UNBLOCK_PLAYER_SPECIFIC_FAILED.create();
                }
                else
                {
                    throw ERROR_BLOCK_PLAYER_SPECIFIC_FAILED.create();
                }
            }
            else
            {
                if (unblock)
                {
                    throw ERROR_UNBLOCK_SERVER_SPECIFIC_FAILED.create();
                }
                else
                {
                    throw ERROR_BLOCK_SERVER_SPECIFIC_FAILED.create();
                }
            }
        }

        stratagemsData.block(holder, unblock);
        PacketUtils.sendClientUpdateStratagemPacket(server, serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));

        if (isPlayer)
        {
            source.sendSuccess(() -> Component.translatable(unblock ? "commands.stratagem.unblock.player.success" : "commands.stratagem.block.player.success", serverPlayer.getDisplayName(), StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        }
        else
        {
            source.sendSuccess(() -> Component.translatable(unblock ? "commands.stratagem.unblock.server.success" : "commands.stratagem.block.server.success", StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        }
        return 1;
    }

    private static int blockAllServerStratagem(CommandSourceStack source, boolean unblock)
    {
        var server = source.getServer();
        var stratagemsData = server.overworld().stratagemsData();

        stratagemsData.block(unblock);

        for (var instance : stratagemsData.listInstances())
        {
            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.UPDATE, instance);
        }

        source.sendSuccess(() -> Component.translatable(unblock ? "commands.stratagem.unblock.server.everything.success" : "commands.stratagem.block.server.everything.success"), true);
        return 1;
    }

    private static int blockAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, boolean unblock)
    {
        for (var instance : serverPlayer.stratagemsData().listInstances())
        {
            instance.block(source.getServer(), serverPlayer, false, unblock);
            PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, instance);
        }
        source.sendSuccess(() -> Component.translatable(unblock ? "commands.stratagem.unblock.player.everything.success" : "commands.stratagem.block.player.everything.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int setModifierAllStratagems(CommandSourceStack source, StratagemModifier modifier, boolean clear)
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().stratagemsData();

        serverStratagems.modified(modifier, clear);

        for (var instance : serverStratagems.listInstances())
        {
            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.UPDATE, instance);
        }

        server.getPlayerList().getPlayers().forEach(serverPlayer ->
        {
            serverPlayer.stratagemsData().modified(modifier, clear);

            for (var instance : serverPlayer.stratagemsData().listInstances())
            {
                PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, instance);
            }
        });

        source.sendSuccess(() -> Component.translatable(clear ? "commands.stratagem.modifier.clear.everything.success" : "commands.stratagem.modifier.set.everything.success", modifier.getTranslatedName()), true);
        return 1;
    }

    private static int setStratagemModifier(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer, StratagemModifier modifier, boolean clear) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.overworld().stratagemsData();
        var stratagem = holder.value();

        if (StratagemUtils.noneMatch(stratagemsData, holder))
        {
            if (isPlayer)
            {
                if (clear)
                {
                    throw ERROR_CLEAR_MODIFIER_PLAYER_SPECIFIC_FAILED.create();
                }
                else
                {
                    throw ERROR_SET_MODIFIER_PLAYER_SPECIFIC_FAILED.create();
                }
            }
            else
            {
                if (clear)
                {
                    throw ERROR_CLEAR_MODIFIER_SERVER_SPECIFIC_FAILED.create();
                }
                else
                {
                    throw ERROR_SET_MODIFIER_SERVER_SPECIFIC_FAILED.create();
                }
            }
        }

        stratagemsData.modified(holder, modifier, clear);
        PacketUtils.sendClientUpdateStratagemPacket(server, serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));

        if (isPlayer)
        {
            source.sendSuccess(() -> Component.translatable(clear ? "commands.stratagem.modifier.clear.player.success" : "commands.stratagem.modifier.set.player.success", modifier.getTranslatedName(), serverPlayer.getDisplayName(), StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        }
        else
        {
            source.sendSuccess(() -> Component.translatable(clear ? "commands.stratagem.modifier.clear.server.success" : "commands.stratagem.modifier.set.server.success", modifier.getTranslatedName(), StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
        }
        return 1;
    }

    private static int setModifierAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer, StratagemModifier modifier, boolean clear)
    {
        for (var instance : serverPlayer.stratagemsData().listInstances())
        {
            instance.modified(source.getServer(), serverPlayer, false, modifier, clear);
            PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, instance);
        }
        source.sendSuccess(() -> Component.translatable(clear ? "commands.stratagem.modifier.clear.player.everything.success" : "commands.stratagem.modifier.set.player.everything.success", modifier.getTranslatedName(), serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int setModifierAllServerStratagem(CommandSourceStack source, StratagemModifier modifier, boolean clear)
    {
        var server = source.getServer();
        var stratagemsData = server.overworld().stratagemsData();

        stratagemsData.modified(modifier, clear);

        for (var instance : stratagemsData.listInstances())
        {
            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.UPDATE, instance);
        }

        source.sendSuccess(() -> Component.translatable(clear ? "commands.stratagem.modifier.clear.server.everything.success" : "commands.stratagem.modifier.set.server.everything.success", modifier.getTranslatedName()), true);
        return 1;
    }

    private static int resetAllStratagems(CommandSourceStack source)
    {
        var server = source.getServer();
        var serverStratagems = server.overworld().stratagemsData();

        serverStratagems.reset();

        for (var instance : serverStratagems.listInstances())
        {
            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.UPDATE, instance);
        }

        server.getPlayerList().getPlayers().forEach(serverPlayer ->
        {
            serverPlayer.stratagemsData().reset();

            for (var instance : serverPlayer.stratagemsData().listInstances())
            {
                PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, instance);
            }
        });

        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.everything.success"), true);
        return 1;
    }

    private static int resetStratagem(CommandSourceStack source, Holder<Stratagem> holder, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.overworld().stratagemsData();
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
        PacketUtils.sendClientUpdateStratagemPacket(server, serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));

        if (isPlayer)
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.success", serverPlayer.getDisplayName(), StratagemUtils.decorateStratagemName(stratagem.name(), holder)), true);
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
        var stratagemsData = server.overworld().stratagemsData();

        stratagemsData.reset();

        for (var instance : stratagemsData.listInstances())
        {
            PacketUtils.sendClientUpdatePacketS2P(server, UpdateStratagemPacket.Action.UPDATE, instance);
        }

        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.server.everything.success"), true);
        return 1;
    }

    private static int resetAllPlayerStratagem(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        for (var instance : serverPlayer.stratagemsData().listInstances())
        {
            instance.reset(source.getServer(), serverPlayer, false);
            PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, instance);
        }
        source.sendSuccess(() -> Component.translatable("commands.stratagem.reset.player.everything.success", serverPlayer.getDisplayName()), true);
        return 1;
    }

    private static int listPlayerStratagems(CommandSourceStack source, ServerPlayer serverPlayer)
    {
        var stratagemsData = serverPlayer.stratagemsData();

        if (stratagemsData.isEmpty())
        {
            source.sendFailure(Component.translatable("commands.stratagem.list.player.empty", serverPlayer.getDisplayName()));
            return 0;
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.player", serverPlayer.getDisplayName(), stratagemsData.size(), StratagemUtils.decorateStratagemList(stratagemsData.listInstances())), true);
            return stratagemsData.size();
        }
    }

    private static int listServerStratagems(CommandSourceStack source) throws CommandSyntaxException
    {
        var server = source.getServer();
        var stratagemsData = server.overworld().stratagemsData();

        if (stratagemsData.isEmpty())
        {
            throw ERROR_LIST_EMPTY_SERVER.create();
        }
        else
        {
            source.sendSuccess(() -> Component.translatable("commands.stratagem.list.server", stratagemsData.size(), StratagemUtils.decorateStratagemList(stratagemsData.listInstances())), true);
            return stratagemsData.size();
        }
    }

    private static int useStratagem(CommandSourceStack source, Holder<Stratagem> holder, BlockPos blockPos, @Nullable ServerPlayer serverPlayer) throws CommandSyntaxException
    {
        var server = source.getServer();
        var isPlayer = serverPlayer != null;
        var stratagemsData = isPlayer ? serverPlayer.stratagemsData() : server.overworld().stratagemsData();
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

        if (stratagemsData.canUse(holder, serverPlayer))
        {
            var stratagemContext = new StratagemActionContext(serverPlayer, source.getLevel(), blockPos, source.getLevel().random);
            holder.value().action().action(stratagemContext);
            stratagemsData.use(holder, serverPlayer);
        }
        else
        {
            var instance = stratagemsData.instanceByHolder(holder);
            source.sendFailure(Component.translatable("commands.stratagem.use.failed", StratagemUtils.decorateStratagemName(stratagem.name(), holder), instance.state.getTranslationName()));
            return 0;
        }

        PacketUtils.sendClientUpdateStratagemPacket(server, serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));

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
}