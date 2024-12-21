package com.stevekung.stratagems.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.stevekung.stratagems.api.StratagemModifier;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class StratagemModifierArgument implements ArgumentType<StratagemModifier>
{
    private static final Collection<String> EXAMPLES = List.of("randomize");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.stratagemModifier.invalid", object));

    private StratagemModifierArgument()
    {}

    public static StratagemModifierArgument stratagemModifier()
    {
        return new StratagemModifierArgument();
    }

    public static StratagemModifier getStratagemModifier(CommandContext<CommandSourceStack> context, String slot)
    {
        return context.getArgument(slot, StratagemModifier.class);
    }

    @Override
    @SuppressWarnings("deprecation")
    public StratagemModifier parse(StringReader reader) throws CommandSyntaxException
    {
        var string = reader.readUnquotedString();
        var stratagemModifier = StratagemModifier.CODEC.byName(string);

        if (stratagemModifier == null)
        {
            throw ERROR_INVALID_VALUE.createWithContext(reader, string);
        }
        else
        {
            return stratagemModifier;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
    {
        return SharedSuggestionProvider.suggest(Arrays.stream(StratagemModifier.values()).map(StratagemModifier::getSerializedName), suggestionsBuilder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}