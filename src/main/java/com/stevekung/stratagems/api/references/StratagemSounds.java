package com.stevekung.stratagems.api.references;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class StratagemSounds
{
    public static final SoundEvent STRATAGEM_CLICK = register("player.stratagem.click");
    public static final SoundEvent STRATAGEM_SELECT = register("player.stratagem.select");
    public static final SoundEvent STRATAGEM_LAND = register("player.stratagem.land");
    public static final SoundEvent STRATAGEM_LOOP = register("player.stratagem.loop");
    public static final SoundEvent STRATAGEM_FAIL = register("player.stratagem.fail");

    public static void init()
    {
        ModConstants.LOGGER.info("Registering stratagem sounds");
    }

    private static SoundEvent register(String name)
    {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, name, SoundEvent.createVariableRangeEvent(ModConstants.id(name)));
    }
}