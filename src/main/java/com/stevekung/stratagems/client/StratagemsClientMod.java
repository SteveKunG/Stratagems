package com.stevekung.stratagems.client;

import org.slf4j.Logger;

import com.google.common.primitives.Chars;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.api.*;
import com.stevekung.stratagems.api.client.ClientStratagemInstance;
import com.stevekung.stratagems.api.client.StratagemInputManager;
import com.stevekung.stratagems.api.packet.*;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.util.StratagemUtils;
import com.stevekung.stratagems.client.renderer.StratagemPodRenderer;
import com.stevekung.stratagems.registry.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StratagemsClientMod implements ClientModInitializer
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient()
    {
        KeyBindings.init();

        EntityRendererRegistry.register(ModEntities.STRATAGEM_BALL, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.STRATAGEM_POD, StratagemPodRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(StratagemsClientMod::clientTick);
        HudRenderCallback.EVENT.register(StratagemsClientMod::renderHud);

        ClientPlayNetworking.registerGlobalReceiver(SetPlayerStratagemsPacket.TYPE, (payload, context) ->
        {
            var player = context.player();
            var level = context.client().level;
            var playerStratagems = payload.playerEntries();

            if (player.getUUID().equals(payload.uuid()))
            {
                LOGGER.info("Add stratagem from packet to {}", level.getPlayerByUUID(payload.uuid()).getName().getString());
                player.stratagemsData().clear();
                player.stratagemsData().instances().putAll(StratagemUtils.clientEntryToMap(playerStratagems, resourceKey -> level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey)));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SetServerStratagemsPacket.TYPE, (payload, context) ->
        {
            LOGGER.info("Received server stratagem packet");
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.clear();
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.putAll(StratagemUtils.clientEntryToMap(payload.serverEntries(), resourceKey -> context.client().level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey)));
        });

        ClientPlayNetworking.registerGlobalReceiver(ClearStratagemsPacket.TYPE, (payload, context) ->
        {
            var clearServer = payload.server();
            var clearPlayer = payload.player();

            if (clearPlayer)
            {
                var player = context.player();

                if (payload.uuid() != null)
                {
                    if (player.getUUID().equals(payload.uuid()))
                    {
                        player.stratagemsData().clear();
                    }
                }
                else
                {
                    LOGGER.warn("Player UUID should not be null!");
                }
            }
            if (clearServer)
            {
                ModConstants.CLIENT_SERVER_STRATAGEM_LIST.clear();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateStratagemPacket.TYPE, (payload, context) ->
        {
            var entryData = payload.entryData();
            var level = context.client().level;
            var holder = level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(entryData.stratagem());
            var instance = new ClientStratagemInstance(entryData.id(), holder, entryData.inboundDuration(), entryData.duration(), entryData.cooldown(), entryData.lastMaxCooldown(), entryData.maxUse(), entryData.state(), entryData.side(), entryData.shouldDisplay());

            if (entryData.side() == StratagemInstance.Side.SERVER)
            {
                switch (payload.action())
                {
                    case UPDATE -> ModConstants.CLIENT_SERVER_STRATAGEM_LIST.replace(holder, instance);
                    case ADD -> ModConstants.CLIENT_SERVER_STRATAGEM_LIST.put(holder, instance);
                    case REMOVE -> ModConstants.CLIENT_SERVER_STRATAGEM_LIST.remove(holder);
                }
            }
            else
            {
                var player = context.player();
                var uuid = payload.uuid();

                if (uuid != null)
                {
                    if (player.getUUID().equals(uuid))
                    {
                        switch (payload.action())
                        {
                            case UPDATE -> player.stratagemsData().instances().replace(holder, instance);
                            case ADD -> player.stratagemsData().instances().put(holder, instance);
                            case REMOVE -> player.stratagemsData().remove(holder);
                        }
                    }
                }
                else
                {
                    LOGGER.warn("Player UUID should not be null!");
                }
            }
        });
    }

    private static void clientTick(Minecraft minecraft)
    {
        var player = minecraft.player;
        var level = minecraft.level;

        if (level == null || player == null)
        {
            return;
        }

        minecraft.getProfiler().push("stratagemClient");

        if (!minecraft.isPaused() && level.tickRateManager().runsNormally())
        {
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.values().forEach(instance -> instance.tick(player));
        }

        minecraft.getProfiler().pop();

        var manager = StratagemInputManager.getInstance();

        if (KeyBindings.OPEN_STRATAGEMS_MENU.consumeClick())
        {
            manager.setMenuOpen(!manager.isMenuOpen());
            manager.clearSelected();
        }

        var arrowKeySound = false;
        var fail = false;

        if (manager.isMenuOpen())
        {
            if (KeyBindings.STRATAGEMS_UP.consumeClick())
            {
                manager.appendInputCode("w");
                arrowKeySound = true;
            }
            else if (KeyBindings.STRATAGEMS_DOWN.consumeClick())
            {
                manager.appendInputCode("s");
                arrowKeySound = true;
            }
            else if (KeyBindings.STRATAGEMS_LEFT.consumeClick())
            {
                manager.appendInputCode("a");
                arrowKeySound = true;
            }
            else if (KeyBindings.STRATAGEMS_RIGHT.consumeClick())
            {
                manager.appendInputCode("d");
                arrowKeySound = true;
            }

            if (manager.hasInputCode())
            {
                var inputCode = manager.getInputCode();

                if (StratagemInputManager.noneMatch(inputCode, player))
                {
                    manager.clearInputCode();
                    fail = true;
                    LOGGER.info("FAIL");
                }
                if (StratagemInputManager.foundMatchFirst(inputCode, player).isPresent())
                {
                    var instance = StratagemInputManager.getInstanceFromCode(inputCode, player);
                    var stratagem = instance.getStratagem().value();

                    manager.setSelected(instance);

                    if (!stratagem.properties().needThrow())
                    {
                        ClientPlayNetworking.send(new UseReplenishStratagemPacket(manager.getSelected().getResourceKey(), player.level().dimension(), player.blockPosition(), instance.side, player.getUUID()));
                        LOGGER.info("Select replenish {}", instance.getResourceKey().location());
                        manager.clearInputCode();
                        manager.clearSelected();
                        manager.setMenuOpen(false);
                        return;
                    }

                    ClientPlayNetworking.send(new PlayStratagemInputSoundPacket(PlayStratagemInputSoundPacket.SoundType.SELECT));
                    minecraft.getSoundManager().play(new StratagemSoundInstance(player));

                    manager.clearInputCode();
                    manager.setMenuOpen(false);

                    LOGGER.info("Select {}", manager.getSelected().stratagem().name().getString());
                }
            }
        }
        else
        {
            manager.clearInputCode();
        }

        if (manager.hasSelected() && minecraft.options.keyAttack.isDown())
        {
            LOGGER.info("Throwing {}", manager.getSelected().stratagem().name().getString());
            ClientPlayNetworking.send(new SpawnStratagemPacket(manager.getSelected().getResourceKey(), manager.getSelected().side));
            manager.clearSelected();
        }

        if (fail)
        {
            ClientPlayNetworking.send(new PlayStratagemInputSoundPacket(PlayStratagemInputSoundPacket.SoundType.FAIL));
        }
        else
        {
            if (arrowKeySound)
            {
                ClientPlayNetworking.send(new PlayStratagemInputSoundPacket(PlayStratagemInputSoundPacket.SoundType.CLICK, manager.getInputCode().length()));
            }
        }
    }

    private static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        var manager = StratagemInputManager.getInstance();
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;

        if (level == null || player == null)
        {
            return;
        }

        var white = DyeColor.WHITE.getTextColor();
        var gray = DyeColor.GRAY.getTextColor();
        var lightGray = DyeColor.LIGHT_GRAY.getTextColor();
        var grayAlpha = FastColor.ARGB32.color(128, 128, 128, 128);
        var label = Component.translatable("stratagem.menu.label");

        label.append(" || ")
                .append(Component.literal("ALPHA RELEASE")
                        .withStyle(ChatFormatting.AQUA));

        if (FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            label.append(" || ")
                    .append("DEBUG Open: " + manager.isMenuOpen());
        }

        guiGraphics.drawString(minecraft.font, label, 12, 18, white);

        var inputCode = manager.getInputCode();
        var baseX = 40;
        var baseY = 40;
        var baseXIcon = 16;
        var baseYIcon = 42;
        var baseYSecond = 12;
        var baseSpacing = 30;
        var arrowSpacing = 8;
        var index = 0;
        var max = 0;
        var backgroundWidth = 0;
        var backgroundHeight = 0;

        for (var stratagemInstance : StratagemInputManager.all(player))
        {
            if (stratagemInstance instanceof ClientStratagemInstance instance)
            {
                var stratagem = instance.stratagem();
                var stratagemName = stratagem.name();
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var codeMatched = code.startsWith(inputCode) && instance.canUse(player);
                var combinedArrows = new StringBuilder();
                var statusText = Component.empty();
                var nameColor = codeMatched ? white : gray;

                if (instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
                {
                    nameColor = lightGray;
                }

                if (instance.state == StratagemState.UNAVAILABLE)
                {
                    statusText = Component.translatable("stratagem.menu.unavailable");
                }

                if (manager.hasSelected() && instance.side == manager.getSelected().side)
                {
                    var equals = code.equals(manager.getSelected().getCode());

                    if (equals)
                    {
                        statusText = Component.translatable("stratagem.menu.activating");
                    }
                }

                Chars.asList(codeChar).forEach(character -> combinedArrows.append(ModConstants.charToArrow(character)));

                var statusColor = codeMatched ? white : gray;

                if (!instance.isReady())
                {
                    if (instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
                    {
                        statusText = Component.translatable("stratagem.menu.inbound").append(" ").append(StratagemUtils.formatTickDuration(instance.inboundDuration, level));
                        statusColor = lightGray;
                    }
                    if (instance.state == StratagemState.COOLDOWN && instance.cooldown > 0)
                    {
                        statusText = Component.translatable("stratagem.menu.cooldown").append(" ").append(StratagemUtils.formatTickDuration(instance.cooldown, level));
                        statusColor = lightGray;
                    }
                }

                var arrowWidth = minecraft.font.width(combinedArrows.toString());
                var nameWidth = minecraft.font.width(stratagemName);
                var statusWidth = minecraft.font.width(statusText);

                if (arrowWidth > max)
                {
                    max = arrowWidth;
                }
                if (nameWidth > max)
                {
                    max = nameWidth;
                }
                if (statusWidth > max)
                {
                    max = statusWidth;
                }

                if (shouldRender(instance))
                {
                    guiGraphics.drawString(minecraft.font, stratagemName, baseX, baseY + index * baseSpacing, nameColor);

                    if (manager.hasSelected() && instance.side == manager.getSelected().side)
                    {
                        var equals = code.equals(manager.getSelected().getCode());

                        if (equals)
                        {
                            guiGraphics.drawString(minecraft.font, stratagem.name(), baseX, baseY + index * baseSpacing, white);
                            guiGraphics.drawString(minecraft.font, statusText, baseX, baseY + baseYSecond + index * baseSpacing, white);
                        }
                    }
                    else
                    {
                        for (var i = 0; i < codeChar.length; i++)
                        {
                            var arrows = ModConstants.charToArrow(codeChar[i]);

                            if (instance.canUse(player))
                            {
                                guiGraphics.drawString(minecraft.font, arrows, baseX + i * arrowSpacing, baseY + baseYSecond + index * baseSpacing, codeMatched ? white : gray);
                            }
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(statusText.getString()))
                    {
                        guiGraphics.drawString(minecraft.font, statusText, baseX, baseY + baseYSecond + index * baseSpacing, statusColor);
                    }

                    if (codeMatched)
                    {
                        var inputCodeChars = inputCode.toCharArray();

                        for (var i = 0; i < inputCodeChars.length; i++)
                        {
                            guiGraphics.drawString(minecraft.font, ModConstants.charToArrow(inputCodeChars[i]), baseX + i * arrowSpacing, baseY + baseYSecond + index * baseSpacing, gray);
                        }
                    }

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, codeMatched ? 0 : -300);
                    renderIcon(guiGraphics, minecraft, instance, stratagem.display(), index, baseXIcon, baseYIcon, baseSpacing);
                    guiGraphics.pose().popPose();

                    backgroundWidth = 22 + max + 20;
                    backgroundHeight = 24 + index * 30;

                    if (!manager.isMenuOpen())
                    {
                        StratagemMenuRenderUtil.renderBackground(guiGraphics, 12, 38 + index * 30, backgroundWidth, 24, -1, grayAlpha);
                    }
                }
                if (instance.shouldDisplay)
                {
                    index++;
                }
            }
        }
        if (manager.isMenuOpen())
        {
            StratagemMenuRenderUtil.renderBackground(guiGraphics, 12, 38, backgroundWidth, backgroundHeight, -1, grayAlpha);
        }
    }

    private static boolean shouldRender(ClientStratagemInstance instance)
    {
        var manager = StratagemInputManager.getInstance();
        var stratagem = instance.stratagem();
        var code = stratagem.code();
        var equals = manager.hasSelected() && code.equals(manager.getSelected().getCode()) && instance.side == manager.getSelected().side;

        if (manager.isMenuOpen() || instance.shouldDisplay || equals || instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
        {
            return true;
        }
        else if (instance.state == StratagemState.COOLDOWN)
        {
            var currentCooldown = instance.cooldown;
            var maxCooldown = instance.lastMaxCooldown;
            return currentCooldown > maxCooldown - 100 || currentCooldown < 100;
        }
        return false;
    }

    private static void renderIcon(GuiGraphics guiGraphics, Minecraft minecraft, ClientStratagemInstance instance, StratagemDisplay display, int index, int x, int y, int spacing)
    {
        switch (display.type())
        {
            case ITEM -> display.itemStack().ifPresent(itemStack ->
            {
                guiGraphics.renderItem(itemStack, x, y + index * spacing);

                if (display.maxUseAsCount())
                {
                    if (instance.maxUse > 0)
                    {
                        guiGraphics.renderItemDecorations(minecraft.font, itemStack, x, y + index * spacing, String.valueOf(instance.maxUse));
                    }
                }
                else
                {
                    display.displayCountOverride().ifPresent(displayCount -> guiGraphics.renderItemDecorations(minecraft.font, itemStack, x, y + index * spacing, displayCount));
                }
            });
            case TEXTURE -> display.texture().ifPresent(resourceLocation -> guiGraphics.blit(resourceLocation, x, y + index * spacing, 0, 0, 16, 16, 16, 16));
            case PLAYER_ICON -> display.playerIcon().ifPresent(resolvableProfile ->
            {
                var supplier = minecraft.getSkinManager().lookupInsecure(resolvableProfile.gameProfile());
                PlayerFaceRenderer.draw(guiGraphics, supplier.get(), x, y + index * spacing, 16);

                if (display.maxUseAsCount())
                {
                    if (instance.maxUse > 0)
                    {
                        guiGraphics.renderItemDecorations(minecraft.font, new ItemStack(Items.STONE), x, y + index * spacing, String.valueOf(instance.maxUse));
                    }
                }
                else
                {
                    display.displayCountOverride().ifPresent(displayCount -> guiGraphics.renderItemDecorations(minecraft.font, new ItemStack(Items.STONE), x, y + index * spacing, displayCount));
                }
            });
        }
    }
}