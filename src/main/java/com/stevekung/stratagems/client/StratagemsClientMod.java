package com.stevekung.stratagems.client;

import org.slf4j.Logger;

import com.google.common.primitives.Chars;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.StratagemDisplay;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.client.StratagemInputManager;
import com.stevekung.stratagems.api.packet.*;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.references.StratagemSounds;
import com.stevekung.stratagems.api.util.StratagemUtils;
import com.stevekung.stratagems.client.renderer.StratagemPodRenderer;
import com.stevekung.stratagems.registry.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringUtil;
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

        ClientPlayNetworking.registerGlobalReceiver(UpdatePlayerStratagemsPacket.TYPE, (payload, context) ->
        {
            var player = context.player();
            var level = context.client().level;
            var playerStratagems = payload.playerEntries();

            if (player.getUUID().equals(payload.uuid()))
            {
                LOGGER.info("Add stratagem from packet to {}", level.getPlayerByUUID(payload.uuid()).getName().getString());
                player.getStratagems().clear();
                player.getStratagems().putAll(StratagemUtils.entryToMap(playerStratagems, level));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateServerStratagemsPacket.TYPE, (payload, context) ->
        {
            LOGGER.info("Received server stratagem packet");
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.clear();
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.putAll(StratagemUtils.mapToInstance(payload.serverEntries(), resourceKey -> context.client().level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey)));
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
                        player.getStratagems().clear();
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
            var instance = new StratagemInstance(entryData.id(), holder, entryData.inboundDuration(), entryData.duration(), entryData.cooldown(), entryData.remainingUse(), entryData.state(), entryData.side());

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
                            case UPDATE -> player.getStratagems().replace(holder, instance);
                            case ADD -> player.getStratagems().put(holder, instance);
                            case REMOVE -> player.getStratagems().remove(holder);
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
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.values().forEach(instance -> instance.tick(null, player));
        }

        minecraft.getProfiler().pop();

        var manager = StratagemInputManager.getInstance();

        if (KeyBindings.OPEN_STRATAGEMS_MENU.consumeClick())
        {
            manager.setMenuOpen(!manager.isMenuOpen());
            manager.clearCode();
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

                    manager.setSide(instance.side);
                    manager.setSelectedCode(inputCode);
                    manager.setSelected(instance.getResourceKey());

                    if (!stratagem.properties().needThrow())
                    {
                        ClientPlayNetworking.send(new UseReplenishStratagemPacket(manager.getSelected(), instance.side, player.getUUID()));
                        LOGGER.info("Select replenish {}", instance.getResourceKey().location());
                        manager.clearInputCode();
                        manager.clearCode();
                        manager.setMenuOpen(false);
                        return;
                    }

                    minecraft.player.playSound(StratagemSounds.STRATAGEM_SELECT, 0.8f, 1.0f);
                    minecraft.getSoundManager().play(new StratagemSoundInstance(minecraft.player));

                    manager.clearInputCode();
                    manager.setMenuOpen(false);

                    LOGGER.info("Select {}", manager.getSelected().location());
                }
            }
        }
        else
        {
            manager.clearInputCode();
        }

        if (manager.hasSelected() && minecraft.options.keyAttack.isDown())
        {
            LOGGER.info("Throwing {}", manager.getSelected().location());
            ClientPlayNetworking.send(new SpawnStratagemPacket(manager.getSelected(), manager.getSide()));
            manager.clearCode();
        }

        if (fail)
        {
            minecraft.player.playSound(StratagemSounds.STRATAGEM_FAIL, 1f, 1f);
        }
        else
        {
            if (arrowKeySound)
            {
                // this is a little detail in HD2 when you're typing stratagem code and sound pitch increased
                minecraft.player.playSound(StratagemSounds.STRATAGEM_CLICK, 0.5f, 1.0f + 0.025f * manager.getInputCode().length());
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

        var white = FastColor.ARGB32.color(255, 255, 255, 255);
        var gray = FastColor.ARGB32.color(255, 128, 128, 128);
        var grayAlpha = FastColor.ARGB32.color(128, 128, 128, 128);

        guiGraphics.drawString(minecraft.font, "Menu Active: " + manager.isMenuOpen(), guiGraphics.guiWidth() / 2 - "Menu Active: ".length(), 10, white);

        if (manager.hasSelectedCode())
        {
            var index = 0;
            var max = 0;

            for (var stratagementry : StratagemInputManager.all(player))
            {
                var stratagem = stratagementry.stratagem();
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var hasCode = code.startsWith(manager.getSelectedCode());
                var equals = code.equals(manager.getSelectedCode());

                if (equals)
                {
                    guiGraphics.drawString(minecraft.font, stratagem.name(), 32, 20 + index * 30, white);
                }

                var combinedArrows = new StringBuilder();

                if (equals)
                {
                    Chars.asList(codeChar).forEach(character -> combinedArrows.append(ModConstants.charToArrow(character)));
                    guiGraphics.drawString(minecraft.font, Component.translatable("stratagem.menu.activating"), 32, 32 + index * 30, white);
                }

                var arrowWidth = minecraft.font.width(combinedArrows.toString()) + 10;
                var textWidth = minecraft.font.width(stratagem.name());

                if (max < arrowWidth)
                {
                    //max = arrowWidth + 60;
                    max = arrowWidth - 20;
                }
                if (arrowWidth < textWidth)
                {
                    max = textWidth;
                }

                if (equals)
                {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, hasCode ? 0 : -300);
                    var finalIndex = index;
                    var display = stratagem.display();

                    if (display.type() == StratagemDisplay.Type.ITEM)
                    {
                        display.itemStack().ifPresent(itemStack ->
                        {
                            if (display.useRemainingAsCount() && stratagementry.remainingUse > 0)
                            {
                                itemStack.setCount(stratagementry.remainingUse);
                            }
                            guiGraphics.renderItem(itemStack, 8, 24 + finalIndex * 30);
                            guiGraphics.renderItemDecorations(minecraft.font, itemStack, 8, 24 + finalIndex * 30);
                        });
                    }
                    guiGraphics.pose().popPose();
                }

                // broken
                //                    if (index <= 2)
                //                    guiGraphics.fill(4, 12 + index * 32, 12 + max, 48 + index * 28, -1, grayAlpha);

                index++;
            }
        }

        if (manager.isMenuOpen())
        {
            var inputCode = manager.getInputCode();
            var index = 0;
            var max = 0;

            for (var instance : StratagemInputManager.all(player))
            {
                var stratagem = instance.stratagem();
                var stratagemName = stratagem.name();
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var codeMatched = code.startsWith(inputCode) && instance.canUse(null, player);
                var combinedArrows = new StringBuilder();
                var statusText = Component.empty();

                guiGraphics.drawString(minecraft.font, stratagemName, 32, 20 + index * 30, codeMatched ? white : gray);

                if (instance.state == StratagemState.UNAVAILABLE && instance.remainingUse == 0)
                {
                    statusText = Component.translatable("stratagem.menu.unavailable");
                }

                for (var i = 0; i < codeChar.length; i++)
                {
                    var arrows = ModConstants.charToArrow(codeChar[i]);
                    combinedArrows.append(arrows);

                    if (instance.canUse(null, player))
                    {
                        guiGraphics.drawString(minecraft.font, arrows, 32 + i * 8, 32 + index * 30, codeMatched ? white : gray);
                    }
                }

                if (!instance.isReady())
                {
                    if (instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
                    {
                        statusText = Component.translatable("stratagem.menu.inbound").append(" ").append(StratagemUtils.formatTickDuration(instance.inboundDuration, level));
                    }
                    if (instance.state == StratagemState.COOLDOWN && instance.cooldown > 0)
                    {
                        statusText = Component.translatable("stratagem.menu.cooldown").append(" ").append(StratagemUtils.formatTickDuration(instance.cooldown, level));
                    }
                }

                if (!StringUtil.isNullOrEmpty(statusText.getString()))
                {
                    guiGraphics.drawString(minecraft.font, statusText, 32, 32 + index * 30, codeMatched ? white : gray);
                }

                var arrowWidth = minecraft.font.width(combinedArrows.toString()) + 10;
                var nameWidth = minecraft.font.width(stratagemName);
                var statusWidth = minecraft.font.width(statusText);

                if (max < arrowWidth)
                {
                    max = arrowWidth - 20;
                }
                if (arrowWidth < nameWidth)
                {
                    max = nameWidth;
                }
                if (nameWidth < statusWidth)
                {
                    max = statusWidth;
                }

                if (codeMatched)
                {
                    var inputCodeChars = inputCode.toCharArray();

                    for (var i = 0; i < inputCodeChars.length; i++)
                    {
                        guiGraphics.drawString(minecraft.font, ModConstants.charToArrow(inputCodeChars[i]), 32 + i * 8, 32 + index * 30, gray);
                    }
                }

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, codeMatched ? 0 : -300);
                renderIcon(guiGraphics, minecraft, instance, stratagem.display(), index);
                guiGraphics.pose().popPose();
                index++;
            }

            guiGraphics.fill(4, 12, 84 + max, 56 + index * 25, -1, grayAlpha);
        }
    }

    private static void renderIcon(GuiGraphics guiGraphics, Minecraft minecraft, StratagemInstance instance, StratagemDisplay display, int index)
    {
        switch (display.type())
        {
            case ITEM -> display.itemStack().ifPresent(itemStack ->
            {
                guiGraphics.renderItem(itemStack, 8, 22 + index * 30);

                if (display.useRemainingAsCount())
                {
                    if (instance.remainingUse > 0)
                    {
                        guiGraphics.renderItemDecorations(minecraft.font, itemStack, 8, 22 + index * 30, String.valueOf(instance.remainingUse));
                    }
                }
                else
                {
                    display.displayCountOverride().ifPresent(displayCount -> guiGraphics.renderItemDecorations(minecraft.font, itemStack, 8, 22 + index * 30, displayCount));
                }
            });
            case TEXTURE ->
                    display.texture().ifPresent(resourceLocation -> guiGraphics.blit(resourceLocation, 8, 22 + index * 30, 0, 0, 16, 16, 16, 16));
            case PLAYER_ICON -> display.playerIcon().ifPresent(resolvableProfile ->
            {
                var supplier = minecraft.getSkinManager().lookupInsecure(resolvableProfile.gameProfile());
                PlayerFaceRenderer.draw(guiGraphics, supplier.get(), 8, 22 + index * 30, 16);

                if (display.useRemainingAsCount())
                {
                    if (instance.remainingUse > 0)
                    {
                        guiGraphics.renderItemDecorations(minecraft.font, new ItemStack(Items.STONE), 8, 22 + index * 30, String.valueOf(instance.remainingUse));
                    }
                }
                else
                {
                    display.displayCountOverride().ifPresent(displayCount -> guiGraphics.renderItemDecorations(minecraft.font, new ItemStack(Items.STONE), 8, 22 + index * 30, displayCount));
                }
            });
        }
    }
}