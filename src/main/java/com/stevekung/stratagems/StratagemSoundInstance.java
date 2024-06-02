package com.stevekung.stratagems;

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
        if (!StratagemManager.getInstance().hasSelectedStratagem())
        {
            this.stop();
        }
    }
}