package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.StratagemsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class StratagemSounds
{
    public static final SoundEvent STRATAGEM_CLICK = register("player.stratagem.click");
    public static final SoundEvent STRATAGEM_SELECT = register("player.stratagem.select");
    public static final SoundEvent STRATAGEM_THROW = register("player.stratagem.throw");
    public static final SoundEvent STRATAGEM_LOOP = register("player.stratagem.loop");

    public static void init()
    {
        StratagemsMod.LOGGER.info("Registering stratagem sounds");
    }

    private static SoundEvent register(String name)
    {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, name, SoundEvent.createVariableRangeEvent(StratagemsMod.id(name)));
    }
}