/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2023  mhahnFr
 *
 * This file is part of the SecretPathway. This program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.SecretPathway.core.lpc.interpreter;

import mhahnFr.SecretPathway.core.lpc.parser.ast.*;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.gui.editor.suggestions.*;
import mhahnFr.utils.StreamPosition;

import java.util.*;

/**
 * This class acts as a context of interpreted LPC source code.
 *
 * @author mhahnFr
 * @since 21.02.23
 */
public class Context extends Instruction {
    /** The {@code return true;} suggestion.          */
    private static final Suggestion trueReturnSuggestion  = new ValueReturnSuggestion(true);
    /** The {@code return false;} suggestion.         */
    private static final Suggestion falseReturnSuggestion = new ValueReturnSuggestion(false);
    /** The generic value return suggestion.          */
    private static final Suggestion valueReturnSuggestion = new ValueReturnSuggestion();
    /** The {@code return;} suggestion.               */
    private static final Suggestion voidReturnSuggestion  = new ReturnSuggestion();
    /** The ellipsis suggestion.                      */
    private static final Suggestion ellipsisSuggestion    = new PlainSuggestion("...");
    private static final Suggestion thisSuggestion        = new ThisSuggestion();
    /** The parent context.                           */
    private final Context parent;
    /** The inherited contexts.                       */
    private final List<Context> superContexts = new ArrayList<>();
    /** The included contexts.                        */
    private final List<Context> includedContexts = new ArrayList<>();
    /** The instructions found in this scope context. */
    private final NavigableMap<Integer, Instruction> instructions = new TreeMap<>();

    /**
     * Constructs a global scope.
     */
    public Context() {
        this(null, 0);
    }

    /**
     * Constructs a scope context with the given beginning
     * position and inside the given parent.
     *
     * @param parent the parent scope context
     * @param begin  the beginning position
     */
    public Context(final Context parent, final int begin) {
        super(begin);

        this.parent = parent;
    }

    /**
     * Pushes and returns a new scope context.
     *
     * @param begin the beginning position of the new context
     * @return the new context
     */
    public Context pushScope(final int begin) {
        final var newContext = new Context(this, begin);
        instructions.put(begin, newContext);
        return newContext;
    }

    /**
     * Closes this scope context and returns the parent context.
     *
     * @param end the end position
     * @return the parent scope context
     */
    public Context popScope(final int end) {
        setEnd(end);

        return parent;
    }

    /**
     * Adds a {@link Context} to the included ones.
     *
     * @param context the context to be added
     */
    public void addIncludedContext(final Context context) {
        includedContexts.add(context);
    }

    /**
     * Adds a {@link Context} as inherited {@link Context}.
     *
     * @param context the context to be added
     */
    public void addSuperContext(final Context context) {
        superContexts.add(context);
    }

    /**
     * Returns the {@link Definition}s available at the given position.
     *
     * @param at   the position
     * @param type the type
     * @return the available instructions
     */
    private Collection<Suggestion> availableDefinitions(final int at, final SuggestionType type) {
        final var toReturn = new HashSet<Suggestion>();

        for (final var instruction : instructions.entrySet()) {
            if (instruction.getKey() < at) {
                final var value = instruction.getValue();
                if (value instanceof final Definition definition) {
                    toReturn.add(new DefinitionSuggestion(definition.getReturnType(), definition));
                } else if (value instanceof Context && at < value.getEnd()) {
                    toReturn.addAll(((Context) value).availableDefinitions(at, type));
                }
            }
        }

        final var superSuggestions = createSuperSuggestions();
        superSuggestions.forEach(s -> {
            if (s instanceof final DefinitionSuggestion suggestion) {
                for (final var maybeSuggestion : toReturn) {
                    if (maybeSuggestion.getSuggestion().equals(suggestion.getSuggestion())) {
                        suggestion.setIsSuperDefinition(true);
                        break;
                    }
                }
            }
        });
        toReturn.addAll(superSuggestions);

        includedContexts.forEach(c -> toReturn.addAll(c.availableDefinitions(Integer.MAX_VALUE, type)));

        final var definition = queryEnclosingFunction();
        if (definition != null) {
            toReturn.add(thisSuggestion);
            if (definition.isVariadic()) {
                toReturn.add(ellipsisSuggestion);
            }
            if (type == SuggestionType.ANY) {
                if (definition.getReturnType() instanceof final ASTTypeDeclaration declaration &&
                        (declaration.getType() == TokenType.VOID || declaration.getType() == TokenType.BOOL)) {
                    if (declaration.getType() == TokenType.VOID) {
                        toReturn.add(voidReturnSuggestion);
                    } else {
                        toReturn.add(trueReturnSuggestion);
                        toReturn.add(falseReturnSuggestion);
                    }
                } else {
                    toReturn.add(valueReturnSuggestion);
                }
            }
        }

        return toReturn;
    }

    /**
     * Returns the inherited suggestions.
     *
     * @return the super suggestions
     */
    public Collection<Suggestion> createSuperSuggestions() {
        final var toReturn = new HashSet<Suggestion>();

        for (final var inherited : superContexts) {
            toReturn.addAll(inherited.availableDefinitions(Integer.MAX_VALUE, null));
        }
        toReturn.removeIf(s -> s == ellipsisSuggestion    ||
                               s == voidReturnSuggestion  ||
                               s == trueReturnSuggestion  ||
                               s == falseReturnSuggestion ||
                               s == valueReturnSuggestion ||
                               s == thisSuggestion);

        return toReturn;
    }

    /**
     * Returns whether the given position is in the global scope.
     *
     * @param position the position to be checked
     * @return whether the position is in global scope
     */
    private boolean isGlobalScope(final int position) {
        for (final var instruction : instructions.entrySet()) {
            if (instruction.getKey() < position && instruction.getValue().getEnd() > position) {
                return !(instruction.getValue() instanceof Context);
            }
        }
        return true;
    }

    /**
     * Returns the enclosing function of this context.
     * Returns {@code null} if there is no enclosing function.
     *
     * @return the enclosing function
     */
    public FunctionDefinition queryEnclosingFunction() {
        if (parent == null) return null;

        final var result = parent.instructions.lowerEntry(getBegin());
        if (result != null && result.getValue() instanceof final FunctionDefinition definition) {
            return definition;
        } else {
            return parent.queryEnclosingFunction();
        }
    }

    public Instruction queryEnclosingFunction(final int position) {
        for (final var entry : instructions.entrySet()) {
            if (position >= entry.getKey() && position <= entry.getValue().getEnd() && entry.getValue() instanceof Context) {
                return instructions.lowerEntry(entry.getKey()).getValue();
            }
        }
        return null;
    }

    /**
     * Returns a list with the available suggestions at the given position.
     *
     * @param position the position
     * @param type     the desired type
     * @return a list with suggestions
     */
    public List<Suggestion> createSuggestions(final int position, final SuggestionType type) {
        final var toReturn = new ArrayList<Suggestion>();
        if (type.is(SuggestionType.LITERAL)) { return toReturn; }

        if (type.is(SuggestionType.ANY, SuggestionType.IDENTIFIER, SuggestionType.LITERAL_IDENTIFIER)) {
            toReturn.addAll(availableDefinitions(position, type));
        }

        if (type.is(SuggestionType.ANY, SuggestionType.TYPE, SuggestionType.TYPE_MODIFIER)) {
            toReturn.add(new TypeSuggestion(TokenType.OBJECT));
            toReturn.add(new TypeSuggestion(TokenType.ANY));
            toReturn.add(new TypeSuggestion(TokenType.INT_KEYWORD));
            toReturn.add(new TypeSuggestion(TokenType.STRING));
            toReturn.add(new TypeSuggestion(TokenType.CHAR_KEYWORD));
            toReturn.add(new TypeSuggestion(TokenType.SYMBOL_KEYWORD));
            toReturn.add(new TypeSuggestion(TokenType.VOID));
            toReturn.add(new TypeSuggestion(TokenType.BOOL));
        }

        if (type.is(SuggestionType.ANY, SuggestionType.LITERAL_IDENTIFIER)) {
            toReturn.add(new ValueSuggestion(TokenType.NIL));
            toReturn.add(new ValueSuggestion(TokenType.TRUE));
            toReturn.add(new ValueSuggestion(TokenType.FALSE));
        }

        if (isGlobalScope(position)) {
            toReturn.add(new InheritSuggestion());
            toReturn.add(new IncludeSuggestion());

            if (type.is(SuggestionType.ANY, SuggestionType.MODIFIER, SuggestionType.TYPE_MODIFIER)) {
                toReturn.add(new TypeSuggestion(TokenType.PRIVATE));
                toReturn.add(new TypeSuggestion(TokenType.PROTECTED));
                toReturn.add(new TypeSuggestion(TokenType.PUBLIC));
                toReturn.add(new TypeSuggestion(TokenType.OVERRIDE));
                toReturn.add(new TypeSuggestion(TokenType.NOSAVE));
                toReturn.add(new TypeSuggestion(TokenType.DEPRECATED));
            }
        } else {
            if (type.is(SuggestionType.ANY, SuggestionType.IDENTIFIER, SuggestionType.LITERAL_IDENTIFIER)) {
                toReturn.add(new NewSuggestion());
            }
            if (type.is(SuggestionType.ANY)) {
                toReturn.add(new ParenthesizedSuggestion(TokenType.IF));
                toReturn.add(new TrySuggestion());
                toReturn.add(new ParenthesizedSuggestion(TokenType.FOR));
                toReturn.add(new ParenthesizedSuggestion(TokenType.FOREACH));
                toReturn.add(new ParenthesizedSuggestion(TokenType.WHILE));
                toReturn.add(new DoSuggestion());
                toReturn.add(new SwitchSuggestion());
            }
        }

        return toReturn;
    }

    /**
     * Returns the {@link Definition} of the named identifier,
     * as defined in a super context. Returns {@code null} if
     * no definition with the given name exists in the super
     * contexts or there are no super context.
     *
     * @param name the name of the identifier
     * @return the definition of the named identifier
     */
    public Definition getSuperIdentifier(final String name) {
        if (parent != null) {
            return parent.getSuperIdentifier(name);
        }

        for (final var context : superContexts) {
            final var identifier = context.getIdentifier(name, Integer.MAX_VALUE);
            if (identifier != null) {
                return identifier;
            }
        }
        return null;
    }

    public Definition digOutIdentifier(final String name, final int position) {
        final var subEntry = instructions.lowerEntry(position);
        if (subEntry == null) {
            return null;
        }
        if (subEntry.getValue() instanceof final Context subContext) {
            return subContext.digOutIdentifier(name, position);
        }
        return getIdentifier(name, position);
    }

    /**
     * Returns the {@link Definition} of the named identifier,
     * whose use ends at the given position. If it is not found
     * {@code null} is returned.
     *
     * @param name  the name of the searched identifier
     * @param begin the beginning search position
     * @return the found {@link Definition} of the identifier or {@code null}
     */
    public Definition getIdentifier(final String name, final int begin) {
        // First, search in our context.
        for (final var element : instructions.entrySet()) {
            if (element.getKey() < begin                 &&
                element.getValue() instanceof Definition &&
                Objects.equals(((Definition) element.getValue()).getName(), name)) {
                return (Definition) element.getValue();
            }
        }

        // If we haven't found the identifier, ask our parent
        // context, provided we have one.
        if (parent != null) {
            return parent.getIdentifier(name, begin);
        }

        // If we don't have a parent, we might have some
        // included contexts, so search in them.
        for (final var context : includedContexts) {
            final var identifier = context.getIdentifier(name, Integer.MAX_VALUE);
            if (identifier != null) {
                return identifier;
            }
        }

        // Otherwise, we might have super contexts,
        // so search in them.
        return getSuperIdentifier(name);
    }

    /**
     * Adds the given identifier to this context.
     *
     * @param begin the beginning position
     * @param name  the name of the identifier
     * @param type  the type of the identifier
     * @param kind  the AST type of the identifier
     */
    public void addIdentifier(final int begin, final String name, final ASTTypeDefinition type, final ASTType kind) {
        instructions.put(begin, new Definition(begin, name, type, kind));
    }

    /**
     * Adds the given identifier to this context.
     *
     * @param begin the beginning position
     * @param name  the name of the identifier
     * @param type  the type of the identifier
     * @param kind  the AST type of the identifier
     */
    public void addIdentifier(final StreamPosition    begin,
                              final ASTName           name,
                              final ASTTypeDefinition type,
                              final ASTType           kind) {
        addIdentifier(begin.position(), name.getName(), type, kind);
    }

    /**
     * Adds a function to this context. The {@link #pushScope(int) pushed} scope
     * context is returned. The given parameters are added to the pushed scope.
     *
     * @param begin      the beginning position of the function definition
     * @param scopeBegin the beginning position of the function's scope
     * @param name       the name of the function
     * @param returnType the return type of the function
     * @param parameters the parameter definitions of the function
     * @param variadic   whether the function takes variadic arguments
     * @return the new scope context for the contents of the function
     */
    public Context addFunction(final StreamPosition    begin,
                               final StreamPosition    scopeBegin,
                               final ASTName           name,
                               final ASTTypeDefinition returnType,
                               final List<Definition>  parameters,
                               final boolean           variadic) {
        instructions.put(begin.position(), new FunctionDefinition(begin.position(),
                                                                  name.getName(),
                                                                  returnType,
                                                                  parameters,
                                                                  variadic));

        final var newContext = pushScope(scopeBegin.position());
        for (final var parameter : parameters) {
            newContext.instructions.put(parameter.getBegin(), parameter);
        }
        return newContext;
    }
}
