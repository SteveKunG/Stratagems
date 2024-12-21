package com.stevekung.stratagems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.arguments.ArgumentType;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.command.argument.StratagemModifierArgument;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;

@Mixin(ArgumentTypeInfos.class)
public class MixinArgumentTypeInfos
{
    @Shadow
    static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String id, Class<? extends A> argumentClass, ArgumentTypeInfo<A, T> info)
    {
        throw new AssertionError();
    }

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void stratagems$registerCommandArgumentType(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> info)
    {
        register(registry, ModConstants.MOD_ID + ":" + "stratagem_modifier", StratagemModifierArgument.class, SingletonArgumentInfo.contextFree(StratagemModifierArgument::stratagemModifier));
    }
}