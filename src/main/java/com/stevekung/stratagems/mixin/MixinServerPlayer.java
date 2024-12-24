package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.stratagems.api.util.PacketUtils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer
{
    @Inject(method = "teleport", at = @At(value = "INVOKE", target = "net/minecraft/server/players/PlayerList.sendActivePlayerEffects(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void stratagems$sendStratagemsOnChangeDimension(TeleportTransition teleportTransition, CallbackInfoReturnable<Entity> info)
    {
        var serverPlayer = ServerPlayer.class.cast(this);
        PacketUtils.sendClientSetPlayerStratagemsPacket(serverPlayer, serverPlayer.stratagemsData());
    }
}