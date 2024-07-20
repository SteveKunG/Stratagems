package com.stevekung.stratagems.client;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.StratagemManager;
import com.stevekung.stratagems.client.renderer.StratagemPodRenderer;
import com.stevekung.stratagems.packet.SpawnStratagemPacket;
import com.stevekung.stratagems.registry.ModEntities;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.StratagemSounds;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
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
    }

    private static void clientTick(Minecraft minecraft)
    {
        var manager = StratagemManager.getInstance();

        if (KeyBindings.OPEN_STRATAGEMS_MENU.consumeClick())
        {
            manager.setStratagemsMenuOpen(!manager.isStratagemsMenuOpen());
            manager.clearStratagemCode();
        }

        var arrowKeySound = false;
        var fail = false;

        if (manager.isStratagemsMenuOpen())
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
                var stratagemRegistry = minecraft.level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).holders().map(Holder.Reference::value).toList();
                var tempStratagemCode = manager.getTempStratagemCode();

                if (stratagemRegistry.stream().noneMatch(s -> s.code().startsWith(tempStratagemCode)))
                {
                    manager.clearTempStratagemCode();
                    LOGGER.info("FAIL");
                    fail = true;
                }
                if (stratagemRegistry.stream().anyMatch(s -> s.code().equals(tempStratagemCode)))
                {
                    LOGGER.info("SELECT");
                    manager.setSelectedStratagemCode(tempStratagemCode);

                    manager.setSelectedStratagem(stratagemRegistry.stream().filter(s -> s.code().equals(tempStratagemCode)).findFirst().get());
                    LOGGER.info("Select {}", manager.getSelectedStratagem().name().getString());
                    minecraft.player.playSound(StratagemSounds.STRATAGEM_SELECT, 0.8f, 1.0f);
                    minecraft.getSoundManager().play(new StratagemSoundInstance(minecraft.player));

                    manager.clearTempStratagemCode();
                    manager.setStratagemsMenuOpen(false);
                }
            }
        }
        else
        {
            manager.clearTempStratagemCode();
        }

        if (manager.hasSelectedStratagem() && minecraft.options.keyAttack.isDown())
        {
            LOGGER.info("Throwing {}", manager.getSelectedStratagem().name().getString());

            if (minecraft.hitResult != null)
            {
                ClientPlayNetworking.send(new SpawnStratagemPacket(minecraft.level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getKey(manager.getSelectedStratagem()), BlockPos.containing(minecraft.hitResult.getLocation())));
            }

            manager.clearStratagemCode();
        }

        if (fail)
        {
            minecraft.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.8f);
        }
        else
        {
            if (arrowKeySound)
            {
                minecraft.player.playSound(StratagemSounds.STRATAGEM_CLICK, 0.5f, 1.0f);
            }
        }
    }

    private static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        var manager = StratagemManager.getInstance();
        var minecraft = Minecraft.getInstance();

        if (minecraft.level == null)
        {
            return;
        }

        var stratagemRegistry = minecraft.level.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).holders().map(Holder.Reference::value).toList();
        var white = FastColor.ARGB32.color(255, 255, 255, 255);
        var gray = FastColor.ARGB32.color(255, 128, 128, 128);
        var grayAlpha = FastColor.ARGB32.color(128, 128, 128, 128);

        guiGraphics.drawString(minecraft.font, "Menu Active: " + manager.isStratagemsMenuOpen(), guiGraphics.guiWidth() / 2 - "Menu Active: ".length(), 10, white);

        if (manager.hasSelectedStratagemCode())
        {
            var index = 0;
            var max = 0;

            for (var stratagem : stratagemRegistry)
            {
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
                    for (var arrow : codeChar)
                    {
                        var arrows = ModConstants.charToArrow(arrow);
                        combinedArrows.append(arrows);
                    }
                    guiGraphics.drawString(minecraft.font, "Activating", 32, 32 + index * 30, white);
                }

                var arrowWidth = minecraft.font.width(combinedArrows.toString()) + 10;

                if (max < arrowWidth)
                {
                    //max = arrowWidth + 60;
                    max = arrowWidth - 20;
                }

                if (equals)
                {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, hasCode ? 0 : -300);
                    var finalIndex = index;
                    stratagem.icon().ifLeft(itemStack -> guiGraphics.renderItem(itemStack, 8, 24 + finalIndex * 30));
                    guiGraphics.pose().popPose();
                }

                // broken
                //                    if (index <= 2)
                //                    guiGraphics.fill(4, 12 + index * 32, 12 + max, 48 + index * 28, -1, grayAlpha);

                index++;
            }
        }

        if (manager.isStratagemsMenuOpen())
        {
            var tempStratagemCode = manager.getTempStratagemCode();
            var index = 0;
            var max = 0;

            for (var stratagem : stratagemRegistry)
            {
                var code = stratagem.code();
                var codeChar = code.toCharArray();
                var hasCode = code.startsWith(tempStratagemCode);

                guiGraphics.drawString(minecraft.font, stratagem.name(), 32, 20 + index * 30, hasCode ? white : gray);

                var combinedArrows = new StringBuilder();

                for (var i = 0; i < codeChar.length; i++)
                {
                    var arrows = ModConstants.charToArrow(codeChar[i]);
                    combinedArrows.append(arrows);

                    guiGraphics.drawString(minecraft.font, arrows, 32 + i * 8, 32 + index * 30, hasCode ? white : gray);
                }

                var arrowWidth = minecraft.font.width(combinedArrows.toString()) + 10;

                if (max < arrowWidth)
                {
                    //max = arrowWidth + 60;
                    max = arrowWidth - 20;
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
                stratagem.icon().ifLeft(itemStack -> guiGraphics.renderItem(itemStack, 8, 24 + finalIndex * 30));
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