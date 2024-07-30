package com.stevekung.stratagems.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.brigadier.arguments.ArgumentType;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.command.argument.StratagemArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;

@Mixin(ArgumentTypeInfos.class)
public class MixinArgumentTypeInfos
{
    @Shadow
    @Final
    static Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void init(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> info)
    {
        register(registry, "stratagem", StratagemArgument.class, new StratagemArgument.Info());
    }

    @Unique
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String id, Class<? extends A> argumentClass, ArgumentTypeInfo<A, T> info)
    {
        BY_CLASS.put(argumentClass, info);
        return Registry.register(registry, StratagemsMod.id(id), info);
    }
}