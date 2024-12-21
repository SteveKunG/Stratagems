package com.stevekung.stratagems.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stevekung.stratagems.api.StratagemModifier;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.client.StratagemInputManager;
import com.stevekung.stratagems.api.util.StratagemUtils;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer
{
    MixinLocalPlayer()
    {
        super(null, null);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void stratagems$tickJammedText(CallbackInfo info)
    {
        if (this.level().getGameTime() % 16L == 0L)
        {
            for (var instance : StratagemInputManager.all(this))
            {
                if (instance.state == StratagemState.BLOCKED && instance.modifier != StratagemModifier.RANDOMIZE)
                {
                    var stratagem = instance.stratagem();
                    var stratagemName = stratagem.name();
                    instance.setJammedName(StratagemUtils.generateJammedText(stratagemName.getString(), this.random, 0.3, false));
                }
            }
        }
        if (this.level().getGameTime() % 40L == 0L)
        {
            for (var instance : StratagemInputManager.all(this))
            {
                var manager = StratagemInputManager.getInstance();

                if (instance.modifier == StratagemModifier.RANDOMIZE)
                {
                    var stratagem = instance.stratagem();
                    var stratagemName = stratagem.name();
                    instance.setJammedName(StratagemUtils.generateJammedText(stratagemName.getString(), this.random, 0.5, true));

                    if (manager.getSelected() != instance)
                    {
                        instance.setRandomizedCode(StratagemUtils.generateRandomizeStratagemCode(this.random, this.registryAccess()));
                    }
                }
            }
        }
    }
}