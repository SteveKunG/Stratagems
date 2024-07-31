package com.stevekung.stratagems.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StratagemArgument<T> implements ArgumentType<Holder.Reference<T>>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    final ResourceKey<? extends Registry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    public StratagemArgument(CommandBuildContext context, ResourceKey<? extends Registry<T>> registryKey)
    {
        this.registryKey = registryKey;
        this.registryLookup = context.lookupOrThrow(registryKey);
    }

    public static <T> StratagemArgument<T> resource(CommandBuildContext context, ResourceKey<? extends Registry<T>> registryKey)
    {
        return new StratagemArgument<>(context, registryKey);
    }

    @Override
    public Holder.Reference<T> parse(StringReader builder) throws CommandSyntaxException
    {
        var resourceLocation = ResourceLocation.read(builder);
        var resourceKey = ResourceKey.create(this.registryKey, resourceLocation);
        return this.registryLookup.get(resourceKey).orElseThrow(() -> ResourceArgument.ERROR_UNKNOWN_RESOURCE.createWithContext(builder, resourceLocation, this.registryKey.location()));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
    {
        var object = commandContext.getSource();

        if (object instanceof CommandSourceStack commandSourceStack)
        {
            return SharedSuggestionProvider.suggestResource(commandSourceStack.getServer().overworld().getServerStratagemData().getStratagemInstances().stream().map(entry -> entry.getResourceKey().location()), suggestionsBuilder);
        }
        else
        {
            return object instanceof SharedSuggestionProvider sharedSuggestionProvider ? sharedSuggestionProvider.customSuggestion(commandContext) : Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<StratagemArgument<T>, StratagemArgument.Info<T>.Template>
    {
        @Override
        public void serializeToNetwork(StratagemArgument.Info<T>.Template template, FriendlyByteBuf buffer)
        {
            buffer.writeResourceKey(template.registryKey);
        }

        @Override
        public StratagemArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf buffer)
        {
            return new StratagemArgument.Info<T>.Template(buffer.readRegistryKey());
        }

        @Override
        public void serializeToJson(StratagemArgument.Info<T>.Template template, JsonObject json)
        {
            json.addProperty("registry", template.registryKey.location().toString());
        }

        @Override
        public StratagemArgument.Info<T>.Template unpack(StratagemArgument<T> argument)
        {
            return new StratagemArgument.Info<T>.Template(argument.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<StratagemArgument<T>>
        {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(final ResourceKey<? extends Registry<T>> registryKey)
            {
                this.registryKey = registryKey;
            }

            @Override
            public StratagemArgument<T> instantiate(CommandBuildContext context)
            {
                return new StratagemArgument<>(context, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<StratagemArgument<T>, ?> type()
            {
                return Info.this;
            }
        }
    }
}