package com.stevekung.stratagems.mixin;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.datafixers.DSL;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.registry.ModReferences;
import com.stevekung.stratagems.util.CustomDataFixTypes;
import net.minecraft.util.datafix.DataFixTypes;

@Mixin(DataFixTypes.class)
public class MixinDataFixTypes
{
    @Shadow
    static DataFixTypes[] $VALUES;

    @Invoker(value = "<init>")
    private static DataFixTypes create(String name, int ordinal, DSL.TypeReference type)
    {
        throw new IllegalStateException("Unreachable");
    }

    static
    {
        var entry = create("SAVED_DATA_STRATAGEMS", $VALUES.length, ModReferences.SAVED_DATA_STRATAGEMS);
        $VALUES = ArrayUtils.add($VALUES, entry);

        StratagemsMod.LOGGER.info("Added new enum to {}: {}", DataFixTypes.class, CustomDataFixTypes.SAVED_DATA_STRATAGEMS);
    }
}