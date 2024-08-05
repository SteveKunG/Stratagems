package com.stevekung.stratagems.client;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.*;
import com.stevekung.stratagems.client.renderer.StratagemPodRenderer;
import com.stevekung.stratagems.packet.SpawnStratagemPacket;
import com.stevekung.stratagems.packet.UpdatePlayerStratagemsPacket;
import com.stevekung.stratagems.packet.UpdateServerStratagemsPacket;
import com.stevekung.stratagems.packet.UseReplenishStratagemPacket;
import com.stevekung.stratagems.registry.ModEntities;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.StratagemSounds;
import com.stevekung.stratagems.util.StratagemUtils;

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
    public static List<StratagemInstance> CLIENT_STRATAGEM_LIST = Lists.newCopyOnWriteArrayList();

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
                playerStratagems.forEach(entry ->
                {
                    var holder = level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(entry.stratagem());
                    player.getStratagems().put(holder, new StratagemInstance(holder, entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.remainingUse(), entry.state(), entry.side()));
                });
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateServerStratagemsPacket.TYPE, (payload, context) ->
        {
            var serverStratagems = payload.serverEntries();

            LOGGER.info("Received server stratagem packet");

            if (serverStratagems.isEmpty())
            {
                CLIENT_STRATAGEM_LIST.clear();
                LOGGER.info("Remove all server stratagems");
            }
            else
            {
                CLIENT_STRATAGEM_LIST = StratagemUtils.mapToInstance(serverStratagems, resourceKey -> context.client().level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey));
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
            CLIENT_STRATAGEM_LIST.forEach(instance -> instance.tick(null, player));
        }

        minecraft.getProfiler().pop();

        var manager = StratagemInputManager.getInstance();

        if (KeyBindings.OPEN_STRATAGEMS_MENU.consumeClick())
        {
            manager.setMenuOpen(!manager.isMenuOpen());
            manager.clearStratagemCode();
        }

        var arrowKeySound = false;
        var fail = false;

        if (manager.isMenuOpen())
        {
            if (KeyBindings.STRATAGEMS_UP.consumeClick())
            {
                manager.appendTempStratagemCode("w");
                arrowKeySound = true;
            }
            else if (KeyBindings.STRATAGEMS_DOWN.consumeClick())
            {
                manager.appendTempStratagemCode("s");
                arrowKeySound = true;
            }
            else if (KeyBindings.STRATAGEMS_LEFT.consumeClick())
            {
                manager.appendTempStratagemCode("a");
                arrowKeySound = true;
            }
            else if (KeyBindings.STRATAGEMS_RIGHT.consumeClick())
            {
                manager.appendTempStratagemCode("d");
                arrowKeySound = true;
            }

            if (manager.hasTempStratagemCode())
            {
                var tempStratagemCode = manager.getTempStratagemCode();

                if (StratagemInputManager.noneMatch(tempStratagemCode, player))
                {
                    manager.clearTempStratagemCode();
                    fail = true;
                    LOGGER.info("FAIL");
                }
                if (StratagemInputManager.foundMatch(tempStratagemCode, player))
                {
                    var instance = StratagemInputManager.getInstanceFromCode(tempStratagemCode, player);
                    var stratagem = instance.getStratagem().value();

                    manager.setSide(instance.side);
                    manager.setSelectedStratagemCode(tempStratagemCode);
                    manager.setSelectedStratagem(instance.getResourceKey());

                    if (stratagem.properties().needThrow().isPresent() && !stratagem.properties().needThrow().get())
                    {
                        ClientPlayNetworking.send(new UseReplenishStratagemPacket(manager.getSelectedStratagem(), instance.side, player.getUUID()));
                        LOGGER.info("Select replenish {}", instance.getResourceKey().location());
                        manager.clearTempStratagemCode();
                        manager.clearStratagemCode();
                        manager.setMenuOpen(false);
                        return;
                    }

                    minecraft.player.playSound(StratagemSounds.STRATAGEM_SELECT, 0.8f, 1.0f);
                    minecraft.getSoundManager().play(new StratagemSoundInstance(minecraft.player));

                    manager.clearTempStratagemCode();
                    manager.setMenuOpen(false);

                    LOGGER.info("Select {}", manager.getSelectedStratagem().location());
                }
            }
        }
        else
        {
            manager.clearTempStratagemCode();
        }

        if (manager.hasSelectedStratagem() && minecraft.options.keyAttack.isDown())
        {
            LOGGER.info("Throwing {}", manager.getSelectedStratagem().location());
            ClientPlayNetworking.send(new SpawnStratagemPacket(manager.getSelectedStratagem(), manager.getSide()));
            manager.clearStratagemCode();
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
                minecraft.player.playSound(StratagemSounds.STRATAGEM_CLICK, 0.5f, 1.0f + 0.025f * manager.getTempStratagemCode().length());
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

        if (manager.hasSelectedStratagemCode())
        {
            var index = 0;
            var max = 0;

            for (var stratagementry : Iterables.concat(CLIENT_STRATAGEM_LIST, player.getStratagems().values()))
            {
                var stratagem = stratagementry.stratagem();
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var hasCode = code.startsWith(manager.getSelectedStratagemCode());
                var equals = code.equals(manager.getSelectedStratagemCode());

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
                            if (display.useRemainingAsCount() && stratagementry.remainingUse != null && stratagementry.remainingUse > 0)
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
            var tempStratagemCode = manager.getTempStratagemCode();
            var index = 0;
            var max = 0;

            for (var instance : Iterables.concat(CLIENT_STRATAGEM_LIST, player.getStratagems().values()))
            {
                var stratagem = instance.stratagem();
                var stratagemName = stratagem.name();
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var codeMatched = code.startsWith(tempStratagemCode) && instance.canUse(null, player);
                var combinedArrows = new StringBuilder();
                var statusText = Component.empty();

                guiGraphics.drawString(minecraft.font, stratagemName, 32, 20 + index * 30, codeMatched ? white : gray);

                if (instance.state == StratagemState.DEPLETED && instance.remainingUse != null && instance.remainingUse == 0)
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
                        statusText = Component.translatable("stratagem.menu.inbound").append(" ").append(instance.formatTickDuration(instance.inboundDuration, level));
                    }
                    if (instance.state == StratagemState.COOLDOWN && instance.cooldown > 0)
                    {
                        statusText = Component.translatable("stratagem.menu.cooldown").append(" ").append(instance.formatTickDuration(instance.cooldown, level));
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
                    var tempCode = tempStratagemCode.toCharArray();

                    for (var i = 0; i < tempCode.length; i++)
                    {
                        guiGraphics.drawString(minecraft.font, ModConstants.charToArrow(tempCode[i]), 32 + i * 8, 32 + index * 30, gray);
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
                    if (instance.remainingUse != null && instance.remainingUse > 0)
                    {
                        guiGraphics.renderItemDecorations(minecraft.font, itemStack, 8, 22 + index * 30, String.valueOf(instance.remainingUse));
                    }
                }
                else
                {
                    display.displayCountOverride().ifPresent(displayCount -> guiGraphics.renderItemDecorations(minecraft.font, itemStack, 8, 22 + index * 30, displayCount));
                }
            });
            case TEXTURE -> display.texture().ifPresent(resourceLocation -> guiGraphics.blit(resourceLocation, 8, 22 + index * 30, 0, 0, 16, 16, 16, 16));
            case PLAYER_ICON -> display.playerIcon().ifPresent(resolvableProfile ->
            {
                var supplier = minecraft.getSkinManager().lookupInsecure(resolvableProfile.gameProfile());
                PlayerFaceRenderer.draw(guiGraphics, supplier.get(), 8, 22 + index * 30, 16);

                if (display.useRemainingAsCount())
                {
                    if (instance.remainingUse != null && instance.remainingUse > 0)
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