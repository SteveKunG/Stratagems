package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.stratagems.api.packet.UpdatePlayerStratagemsPacket;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer
{
    @Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "net/minecraft/server/players/PlayerList.sendActivePlayerEffects(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void changeDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> info)
    {
        var player = ServerPlayer.class.cast(this);
        player.connection.send(new ClientboundCustomPayloadPacket(UpdatePlayerStratagemsPacket.create(player.stratagemsData(), player.getUUID())));
    }
}