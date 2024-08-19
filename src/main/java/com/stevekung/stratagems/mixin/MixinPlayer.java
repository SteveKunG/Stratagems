package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stevekung.stratagems.api.PlayerStratagemsData;
import com.stevekung.stratagems.api.accessor.StratagemsDataAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity implements StratagemsDataAccessor
{
    @Unique
    private PlayerStratagemsData stratagems;

    MixinPlayer()
    {
        super(null, null);
    }

    @Override
    public PlayerStratagemsData stratagemsData()
    {
        return this.stratagems;
    }

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void init(CallbackInfo info)
    {
        this.stratagems = new PlayerStratagemsData(Player.class.cast(this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo info)
    {
        this.stratagems.tick();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addStratagemSaveData(CompoundTag compound, CallbackInfo info)
    {
        this.stratagems.save(compound);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(CompoundTag compound, CallbackInfo info)
    {
        this.stratagems.load(compound);
    }
}