package com.stevekung.stratagems.client;

import org.slf4j.Logger;

import com.google.common.primitives.Chars;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.StratagemDisplay;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemState;
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
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.ShaderInstance;
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
    private static float animationTime;
    private static boolean visible;
    private static final float xStart = -200f;
    private static final float xStop = 0f;
    private static final float speed = 100f;

    private static ShaderInstance staticNoiseShader;

    @Override
    public void onInitializeClient()
    {
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(ModConstants.id("static_noise"), DefaultVertexFormat.POSITION_TEX, program -> staticNoiseShader = program));

        KeyBindings.init();

        EntityRendererRegistry.register(ModEntities.STRATAGEM_BALL, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.STRATAGEM_POD, StratagemPodRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(StratagemsClientMod::clientTick);

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
            ModConstants.CLIENT_SERVER_STRATAGEM_LIST.putAll(StratagemUtils.clientMapToInstance(payload.serverEntries(), resourceKey -> context.client().level.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(resourceKey)));
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
                var optionalInstance = StratagemInputManager.getInstanceFromCode(inputCode, player);

                if (StratagemInputManager.noneMatch(inputCode, player))
                {
                    manager.clearInputCode();
                    fail = true;
                    LOGGER.info("FAIL");
                }
                if (optionalInstance.isPresent())
                {
                    var instance = optionalInstance.get();
                    var stratagem = instance.getStratagem().value();

                    instance.animationTime = 0f;
                    instance.visible = true;
                    instance.selected = true;
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

                    manager.setMenuOpen(false);

                    LOGGER.info("Select {}", manager.getSelected().stratagem().name().getString());
                }
            }

            for (var instance : StratagemInputManager.all(player))
            {
                if (!instance.visible)
                {
                    // Clear selected stratagem after open menu
                    if (instance.selected)
                    {
                        instance.selected = false;
                    }

                    instance.visible = true;
                    instance.animationTime = xStart;
                }
            }
        }
        else
        {
            for (var instance : StratagemInputManager.all(player))
            {
                if (instance.visible)
                {
                    instance.visible = false;
                    instance.animationTime = xStop;
                }
            }
            manager.clearInputCode();
        }

        if (manager.hasSelected() && minecraft.options.keyAttack.isDown())
        {
            LOGGER.info("Throwing {}", manager.getSelected().stratagem().name().getString());
            ClientPlayNetworking.send(new SpawnStratagemPacket(manager.getSelected().getResourceKey(), manager.getSelected().side));
            manager.clearInputCode();
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

    @SuppressWarnings("incomplete-switch")
    public static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
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

        if (manager.isMenuOpen() && !visible)
        {
            visible = true;
            animationTime = xStart;
        }
        if (!manager.isMenuOpen() && visible)
        {
            visible = false;
            animationTime = xStop;
        }

        if (animationTime >= xStart && animationTime <= xStop)
        {
            if (visible)
            {
                animationTime += deltaTracker.getGameTimeDeltaTicks() * speed;

                if (animationTime > xStop)
                {
                    animationTime = xStop;
                }
            }
            else
            {
                animationTime -= deltaTracker.getGameTimeDeltaTicks() * speed;
            }
        }

        for (var instance : StratagemInputManager.all(player))
        {
            var stratagem = instance.stratagem();
            var isBlocked = instance.state == StratagemState.BLOCKED;
            var stratagemName = isBlocked ? Component.literal(instance.getJammedName()) : stratagem.name();
            var code = stratagem.code();
            var codeChar = code.toCharArray();
            var codeMatched = code.startsWith(inputCode) && instance.canUse(player);
            var combinedArrows = new StringBuilder();
            var statusText = Component.empty();
            var textColor = codeMatched ? white : gray;

            if (!instance.selected && instance.animationTime >= xStart && instance.animationTime <= xStop)
            {
                if (instance.visible)
                {
                    instance.animationTime += deltaTracker.getGameTimeDeltaTicks() * speed;

                    if (instance.animationTime > xStop)
                    {
                        instance.animationTime = xStop;
                    }
                }
                else
                {
                    instance.animationTime -= deltaTracker.getGameTimeDeltaTicks() * speed;
                }
            }

            switch (instance.state)
            {
                case COOLDOWN -> {
                    var currentCooldown = instance.cooldown;
                    var maxCooldown = instance.lastMaxCooldown;

                    if (currentCooldown > maxCooldown - 100 || currentCooldown < 100)
                    {
                        instance.animationTime = xStop;
                    }

                    if (!instance.isReady() && instance.cooldown > 0)
                    {
                        statusText = Component.translatable("stratagem.menu.cooldown").append(" ").append(StratagemUtils.formatTickDuration(instance.cooldown, level));
                        textColor = lightGray;
                    }
                }
                case INBOUND -> {
                    if (instance.inboundDuration > 0)
                    {
                        textColor = lightGray;
                        instance.animationTime = xStop;

                        if (!instance.isReady())
                        {
                            statusText = Component.translatable("stratagem.menu.inbound").append(" ").append(StratagemUtils.formatTickDuration(instance.inboundDuration, level));
                            textColor = lightGray;
                        }
                    }
                }
                case UNAVAILABLE -> statusText = Component.translatable("stratagem.menu.unavailable");
                case BLOCKED -> statusText = Component.translatable("stratagem.menu.jammed");
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

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(instance.animationTime, 0, 0);

            if (shouldRenderForMenu(instance))
            {
                guiGraphics.drawString(minecraft.font, stratagemName, baseX, baseY + index * baseSpacing, textColor);

                if (manager.hasSelected() && instance.side == manager.getSelected().side)
                {
                    var equals = code.equals(manager.getSelected().getCode());

                    if (equals)
                    {
                        guiGraphics.drawString(minecraft.font, stratagemName, baseX, baseY + index * baseSpacing, white);
                    }
                }
                else
                {
                    for (var i = 0; i < codeChar.length; i++)
                    {
                        var arrows = ModConstants.charToArrow(codeChar[i]);

                        if (instance.canUse(player))
                        {
                            guiGraphics.drawString(minecraft.font, arrows, baseX + i * arrowSpacing, baseY + baseYSecond + index * baseSpacing, textColor);
                        }
                    }
                }

                if (!StringUtil.isNullOrEmpty(statusText.getString()))
                {
                    guiGraphics.drawString(minecraft.font, statusText, baseX, baseY + baseYSecond + index * baseSpacing, textColor);
                }

                if (codeMatched && !manager.hasSelected())
                {
                    var inputCodeChars = inputCode.toCharArray();

                    for (var i = 0; i < inputCodeChars.length; i++)
                    {
                        guiGraphics.drawString(minecraft.font, ModConstants.charToArrow(inputCodeChars[i]), baseX + i * arrowSpacing, baseY + baseYSecond + index * baseSpacing, gray);
                    }
                }

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, codeMatched ? 0 : -300);

                if (isBlocked)
                {
                    renderStaticNoiseShader(guiGraphics, baseXIcon, baseYIcon + index * baseSpacing);
                }
                else
                {
                    renderIcon(guiGraphics, minecraft, instance, stratagem.display(), baseXIcon, baseYIcon + index * baseSpacing, isBlocked);
                }

                guiGraphics.pose().popPose();

                backgroundWidth = 22 + max + 20;
                backgroundHeight = 24 + index * 30;
            }

            if (!manager.isMenuOpen() && shouldRenderSingleBackground(instance))
            {
                StratagemMenuRenderUtil.renderBackground(guiGraphics, 12, 38 + index * 30, backgroundWidth, 24, -1, grayAlpha, false);
            }

            guiGraphics.pose().popPose();

            if (instance.shouldDisplay)
            {
                index++;
            }
        }
        if (animationTime > -150f)
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(animationTime, 0, 0);
            StratagemMenuRenderUtil.renderBackground(guiGraphics, 12, 38, backgroundWidth, backgroundHeight, -1, grayAlpha, true);
            guiGraphics.pose().popPose();
        }
    }

    private static boolean shouldRenderForMenu(ClientStratagemInstance instance)
    {
        return instance.animationTime > -180f;
    }

    private static boolean shouldRenderSingleBackground(ClientStratagemInstance instance)
    {
        var manager = StratagemInputManager.getInstance();
        var stratagem = instance.stratagem();
        var code = stratagem.code();
        var equals = manager.hasSelected() && code.equals(manager.getSelected().getCode()) && instance.side == manager.getSelected().side;

        if (animationTime < -180f && instance.shouldDisplay || equals || instance.state == StratagemState.INBOUND && instance.inboundDuration > 0)
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

    private static void renderStaticNoiseShader(GuiGraphics guiGraphics, int x, int y)
    {
        var size = 16;
        var zOffset = 300;

        RenderSystem.setShader(() -> staticNoiseShader);
        var matrix4f = guiGraphics.pose().last().pose();
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix4f, x, y, zOffset).setUv(0.0F, 0.0F); // top left
        buffer.addVertex(matrix4f, x, y + size, zOffset).setUv(1.0F, 0.0F); // bottom left
        buffer.addVertex(matrix4f, x + size, y + size, zOffset).setUv(1.0F, 1.0F); // bottom right
        buffer.addVertex(matrix4f, x + size, y, zOffset).setUv(0.0F, 1.0F); // top right
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderIcon(GuiGraphics guiGraphics, Minecraft minecraft, ClientStratagemInstance instance, StratagemDisplay display, int x, int y, boolean isBlocked)
    {
        switch (display.type())
        {
            case ITEM -> display.itemStack().ifPresent(itemStack ->
            {
                guiGraphics.renderItem(itemStack, x, y);
                renderDecoratedCount(guiGraphics, itemStack, minecraft, instance, display, x, y, isBlocked);
            });
            case TEXTURE -> display.texture().ifPresent(resourceLocation ->
            {
                guiGraphics.blit(resourceLocation, x, y, 0, 0, 16, 16, 16, 16);
                renderDecoratedCount(guiGraphics, new ItemStack(Items.STONE), minecraft, instance, display, x, y, isBlocked);
            });
            case PLAYER_ICON -> display.playerIcon().ifPresent(resolvableProfile ->
            {
                var supplier = minecraft.getSkinManager().lookupInsecure(resolvableProfile.gameProfile());
                PlayerFaceRenderer.draw(guiGraphics, supplier.get(), x, y, 16);
                renderDecoratedCount(guiGraphics, new ItemStack(Items.STONE), minecraft, instance, display, x, y, isBlocked);
            });
        }
    }

    private static void renderDecoratedCount(GuiGraphics guiGraphics, ItemStack itemStack, Minecraft minecraft, ClientStratagemInstance instance, StratagemDisplay display, int x, int y, boolean isBlocked)
    {
        if (isBlocked)
        {
            return;
        }
        if (display.maxUseAsCount())
        {
            if (instance.maxUse > 0)
            {
                guiGraphics.renderItemDecorations(minecraft.font, itemStack, x, y, String.valueOf(instance.maxUse));
            }
        }
        else
        {
            display.displayCountOverride().ifPresent(displayCount -> guiGraphics.renderItemDecorations(minecraft.font, itemStack, x, y, displayCount));
        }
    }
}