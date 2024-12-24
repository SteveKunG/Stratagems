package com.stevekung.stratagems.mixin;

import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.stevekung.stratagems.api.references.ModReferences;

import net.minecraft.util.datafix.schemas.V1460;
import net.minecraft.util.datafix.schemas.V99;

@Mixin(value = { V1460.class, V99.class})
public class MixinDataFixSchemas
{
    @Inject(method = "registerTypes", at = @At("TAIL"))
    private void stratagems$registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2, CallbackInfo info)
    {
        schema.registerType(false, ModReferences.SAVED_DATA_STRATAGEMS, DSL::remainder);
    }
}