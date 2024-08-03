package com.stevekung.stratagems.client;

import org.slf4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Chars;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemMenuManager;
import com.stevekung.stratagems.StratagemState;
import com.stevekung.stratagems.client.renderer.StratagemPodRenderer;
import com.stevekung.stratagems.packet.SpawnStratagemPacket;
import com.stevekung.stratagems.packet.UpdateStratagemsPacket;
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
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

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

        ClientPlayNetworking.registerGlobalReceiver(UpdateStratagemsPacket.TYPE, (payload, context) ->
        {
            var player = context.player();
            var serverStratagem = payload.serverEntries();
            var playerStratagem = payload.playerEntries();

            LOGGER.info("Received server stratagem packet");

            if (serverStratagem.isEmpty())
            {
                StratagemUtils.CLIENT_STRATAGEM_LIST.clear();
                LOGGER.info("Remove all server stratagems");
            }
            else
            {
                StratagemUtils.CLIENT_STRATAGEM_LIST = UpdateStratagemsPacket.mapEntryToInstance(serverStratagem, context.client().level.registryAccess());
            }

            if (player.getUUID().equals(payload.uuid()))
            {
                LOGGER.info("Add stratagem from packet to {}", context.client().level.getPlayerByUUID(payload.uuid()).getName().getString());

                player.getPlayerStratagems().clear();
                playerStratagem.forEach(entry ->
                {
                    var holder = context.client().level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(entry.stratagem());
                    context.player().getPlayerStratagems().put(holder, new StratagemInstance(holder, entry.inboundDuration(), entry.duration(), entry.cooldown(), entry.remainingUse(), entry.state(), entry.side()));
                });
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
            StratagemUtils.CLIENT_STRATAGEM_LIST.forEach(t -> t.tick(null, player));
        }

        minecraft.getProfiler().pop();

        var manager = StratagemMenuManager.getInstance();

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

                if (StratagemUtils.clientNoneMatch(tempStratagemCode, player))
                {
                    manager.clearTempStratagemCode();
                    fail = true;
                    LOGGER.info("FAIL");
                }
                if (StratagemUtils.clientFoundMatch(tempStratagemCode, player))
                {
                    var instance = StratagemUtils.getStratagemFromCode(tempStratagemCode, player);
                    var holder = instance.getStratagem();

                    manager.setSide(instance.side);
                    manager.setSelectedStratagemCode(tempStratagemCode);
                    manager.setSelectedStratagem(holder.unwrapKey().orElseThrow());

                    if (holder.value().properties().needThrow().isPresent() && !holder.value().properties().needThrow().get())
                    {
                        ClientPlayNetworking.send(new UseReplenishStratagemPacket(manager.getSelectedStratagem().location(), instance.side, player.getUUID()));
                        LOGGER.info("Select replenish {}", holder.unwrapKey().orElseThrow().location());
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
            ClientPlayNetworking.send(new SpawnStratagemPacket(manager.getSelectedStratagem().location(), manager.getSide()));
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
                // this is a little detail in HD2 when you typing stratagem code and pitch increased
                minecraft.player.playSound(StratagemSounds.STRATAGEM_CLICK, 0.5f, 1.0f + (0.025f * manager.getTempStratagemCode().length()));
            }
        }
    }

    private static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        var manager = StratagemMenuManager.getInstance();
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

            for (var stratagementry : Iterables.concat(StratagemUtils.CLIENT_STRATAGEM_LIST, player.getPlayerStratagems().values()))
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
                    stratagem.icon().ifLeft(itemStack ->
                    {
                        if (stratagementry.remainingUse != null && stratagementry.remainingUse > 0)
                        {
                            itemStack.setCount(stratagementry.remainingUse);
                        }
                        guiGraphics.renderItem(itemStack, 8, 24 + finalIndex * 30);
                        guiGraphics.renderItemDecorations(minecraft.font, itemStack, 8, 24 + finalIndex * 30);
                    });
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

            for (var stratagementry : Iterables.concat(StratagemUtils.CLIENT_STRATAGEM_LIST, player.getPlayerStratagems().values()))
            {
                var stratagem = stratagementry.stratagem();
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var hasCode = code.startsWith(tempStratagemCode) && stratagementry.canUse(null, player);

                guiGraphics.drawString(minecraft.font, stratagem.name(), 32, 20 + index * 30, hasCode ? white : gray);

                var combinedArrows = new StringBuilder();

                if (stratagementry.state == StratagemState.DEPLETED && stratagementry.remainingUse != null && stratagementry.remainingUse == 0)
                {
                    guiGraphics.drawString(minecraft.font, Component.translatable("stratagem.menu.unavailable"), 32, 32 + index * 30, hasCode ? white : gray);
                }

                for (var i = 0; i < codeChar.length && stratagementry.canUse(null, player); i++)
                {
                    var arrows = ModConstants.charToArrow(codeChar[i]);
                    combinedArrows.append(arrows);

                    if (stratagementry.isReady())
                    {
                        guiGraphics.drawString(minecraft.font, arrows, 32 + i * 8, 32 + index * 30, hasCode ? white : gray);
                    }
                }

                if (!stratagementry.isReady())
                {
                    if (stratagementry.state == StratagemState.INBOUND && stratagementry.inboundDuration > 0)
                    {
                        guiGraphics.drawString(minecraft.font, Component.translatable("stratagem.menu.inbound").getString() + " " + stratagementry.formatTickDuration(stratagementry.inboundDuration, level), 32, 32 + index * 30, hasCode ? white : gray);
                    }
                    if (stratagementry.state == StratagemState.COOLDOWN && stratagementry.cooldown > 0)
                    {
                        guiGraphics.drawString(minecraft.font, Component.translatable("stratagem.menu.cooldown").getString() + " " + stratagementry.formatTickDuration(stratagementry.cooldown, level), 32, 32 + index * 30, hasCode ? white : gray);
                    }
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

                if (hasCode)
                {
                    var tempCode = tempStratagemCode.toCharArray();

                    for (var i = 0; i < tempCode.length; i++)
                    {
                        guiGraphics.drawString(minecraft.font, ModConstants.charToArrow(tempCode[i]), 32 + i * 8, 32 + index * 30, gray);
                    }
                }

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, hasCode ? 0 : -300);
                var finalIndex = index;
                stratagem.icon().ifLeft(itemStack ->
                {
                    if (stratagementry.remainingUse != null && stratagementry.remainingUse > 0)
                    {
                        itemStack.setCount(stratagementry.remainingUse);
                    }

                    guiGraphics.renderItem(itemStack, 8, 24 + finalIndex * 30);

                    if (stratagementry.remainingUse != null)
                    {
                        guiGraphics.renderItemDecorations(minecraft.font, itemStack.copyWithCount(stratagementry.remainingUse), 8, 24 + finalIndex * 30, String.valueOf(itemStack.getCount()));
                    }
                });
                guiGraphics.pose().popPose();

                // broken
                //                    if (index <= 2)
                //                    guiGraphics.fill(4, 12 + index * 32, 12 + max, 48 + index * 28, -1, grayAlpha);

                index++;
            }

            guiGraphics.fill(4, 12, 84 + max, 56 + index * 24, -1, grayAlpha);
        }
    }
}