package com.stevekung.stratagems.client;

import com.stevekung.stratagems.StratagemInputManager;
import com.stevekung.stratagems.registry.StratagemSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class StratagemSoundInstance extends AbstractTickableSoundInstance
{
    public StratagemSoundInstance(Player player)
    {
        super(StratagemSounds.STRATAGEM_LOOP, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick()
    {
        if (!StratagemInputManager.getInstance().hasSelectedStratagem())
        {
            this.stop();
        }
    }
}