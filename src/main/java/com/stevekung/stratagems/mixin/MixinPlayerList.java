package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.stevekung.stratagems.packet.UpdatePlayerStratagemsPacket;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;

@Mixin(PlayerList.class)
public class MixinPlayerList
{
    @Inject(method = "respawn", at = @At("TAIL"))
    private void respawn(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> info, @Local(index = 6, ordinal = 1) ServerPlayer serverPlayer)
    {
        serverPlayer.getPlayerStratagems().putAll(player.getPlayerStratagems());
        ServerPlayNetworking.send(serverPlayer, UpdatePlayerStratagemsPacket.create(player.getPlayerStratagems().values(), player.getUUID()));
    }
}