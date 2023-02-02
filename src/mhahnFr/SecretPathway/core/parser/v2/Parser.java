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

package mhahnFr.SecretPathway.core.parser.v2;

import mhahnFr.SecretPathway.core.parser.ast.*;
import mhahnFr.SecretPathway.core.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.parser.tokenizer.Tokenizer;
import mhahnFr.utils.StreamPosition;
import mhahnFr.utils.StringStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

/**
 * This class parses LPC source code.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class Parser {
    private final Tokenizer tokenizer;
    private Token previous;
    private Token current;
    private Token next;

    public Parser(final String source) {
        final var stream = new StringStream(source);
        tokenizer = new Tokenizer(stream);
        advance(2);
        previous = new StartToken(stream.createStreamPosition(0));
    }

    private void advance(final int count) {
        for (int i = 0; i < count; ++i) {
            advance();
        }
    }

    private void advance() {
        previous = current;
        current  = next;
        next     = tokenizer.nextToken();
    }

    private ASTExpression combine(ASTExpression main, Collection<ASTExpression> parts) {
        return combine(main, parts.toArray(new ASTExpression[0]));
    }

    private ASTExpression combine(ASTExpression main, ASTExpression... parts) {
        final var array = new ASTExpression[parts.length + 1];
        array[0] = main;
        System.arraycopy(parts, 0, array, 1, parts.length);
        return new ASTCombination(array);
    }

    private ASTExpression parseInclude() {
        final ASTExpression toReturn;

        advance();
        if (current.type() != TokenType.STRING) {
            return combine(new ASTInclude(previous.beginPos(), current.beginPos(), null),
                           new ASTMissing(previous.endPos(), current.beginPos(), "Expected a string literal"));
        } else {
            toReturn = new ASTInclude(previous.beginPos(), current.endPos(), (String) current.payload());
        }

        advance();
        return toReturn;
    }

    private ASTExpression parseInherit() {
        final ASTExpression toReturn;

        advance();
        if (current.type() == TokenType.SEMICOLON) {
            toReturn = new ASTInheritance(previous.beginPos(), current.endPos(), null);
        } else if (next.type() == TokenType.SEMICOLON && current.type() != TokenType.STRING) {
            toReturn = combine(new ASTInheritance(previous.beginPos(), next.endPos(), null),
                               new ASTWrong(current, "Expected a string literal"));
            advance();
        } else if (current.type() == TokenType.STRING && next.type() != TokenType.SEMICOLON) {
            toReturn = combine(new ASTInheritance(previous.beginPos(), current.endPos(), (String) current.payload()),
                               new ASTMissing(current.endPos(), next.beginPos(), "Expected ';'"));
        } else if (current.type() != TokenType.SEMICOLON && next.type() != TokenType.SEMICOLON) {
            return combine(new ASTInheritance(previous.beginPos(), current.beginPos(), null),
                           new ASTMissing(previous.endPos(), current.beginPos(), "Expected ';'"));
        } else {
            toReturn = new ASTInheritance(previous.beginPos(), next.endPos(), (String) current.payload());
            advance();
        }
        advance();
        return toReturn;
    }

    private ASTExpression parseClass() {
        return null;
    }

    private boolean isModifier(final TokenType type) {
        return type == TokenType.PRIVATE    ||
               type == TokenType.PROTECTED  ||
               type == TokenType.PUBLIC     ||
               type == TokenType.DEPRECATED ||
               type == TokenType.OVERRIDE   ||
               type == TokenType.NOSAVE;
    }

    private Collection<TokenType> parseModifiers() {
        final var toReturn = new Vector<TokenType>();

        while (true) {
            if (isModifier(current.type())) {
                toReturn.add(current.type());
            } else if (isModifier(next.type())) {
                // wrong, consume as modifier
            } else {
                break;
            }
            advance();
        }

        return toReturn;
    }

    private boolean isType(final TokenType type) {
        return type == TokenType.VOID           ||
               type == TokenType.CHAR_KEYWORD   ||
               type == TokenType.INT_KEYWORD    ||
               type == TokenType.BOOL           ||
               type == TokenType.OBJECT         ||
               type == TokenType.STRING_KEYWORD ||
               type == TokenType.SYMBOL_KEYWORD ||
               type == TokenType.MAPPING        ||
               type == TokenType.ANY            ||
               type == TokenType.MIXED          ||
               type == TokenType.AUTO           ||
               type == TokenType.OPERATOR;
    }

    private ASTExpression parseType() {
        final ASTExpression toReturn;

        if (!isType(current.type()) && next.type() == TokenType.IDENTIFIER) {
            toReturn = combine(new ASTTypeDeclaration(null),
                               new ASTWrong(current, "Expected a type"));
            advance();
        } else if (current.type() == TokenType.IDENTIFIER) {
            toReturn = combine(new ASTTypeDeclaration(null),
                               new ASTMissing(previous.endPos(), current.beginPos(), "Missing type"));
        } else {
            toReturn = new ASTTypeDeclaration(current);
            advance();
        }

        return toReturn;
    }

    private String parseName() {
        final String toReturn;

        if (current.type() == TokenType.LEFT_PAREN ||
            current.type() == TokenType.SEMICOLON  ||
            current.type() == TokenType.EQUALS) {
            // missing name
            toReturn = null;
        } else if (current.type() != TokenType.IDENTIFIER) {
            // wrong, consume as name
            toReturn = null;
            advance();
        } else {
            toReturn = (String) current.payload();
            advance();
        }

        return toReturn;
    }

    private ASTExpression parseVariableDefinition(final Collection<TokenType> modifiers,
                                                  final ASTExpression         type,
                                                  final String                name) {
        return null;
    }

    private ASTExpression parseFunctionDefinition(final Collection<TokenType> modifiers,
                                                  final ASTExpression         type,
                                                  final String                name) {
        return null;
    }

    private ASTExpression parseFileExpression() {
        // modifiers type name
        final var modifiers = parseModifiers();
        final var type      = parseType();
        final var name      = parseName();

        if (current.type() == TokenType.LEFT_PAREN) {
            // def. func
            return parseFunctionDefinition(modifiers, type, name);
        } else if (current.type() == TokenType.SEMICOLON) {
            // def. var
            return parseVariableDefinition(modifiers, type, name);
        } else {
            // TODO
        }
        return null;
    }

    private ASTExpression parseExpression() {
        if (current.type() == TokenType.INCLUDE) {
            return parseInclude();
        } else if (current.type() == TokenType.INHERIT) {
            return parseInherit();
        } else if (current.type() == TokenType.CLASS) {
            return parseClass();
        }
        return parseFileExpression();
    }

    private ASTExpression[] parse(final TokenType end) {
        final var expressions = new ArrayList<ASTExpression>();

        while (current.type() != TokenType.EOF && current.type() != end) {
            expressions.add(parseExpression());
        }

        return expressions.toArray(new ASTExpression[0]);
    }

    public ASTExpression[] parse() {
        return parse(TokenType.EOF);
    }

    /**
     * This class represents a dummy starting token.
     *
     * @author mhahnFr
     * @since 02.02.23
     */
    private static class StartToken extends Token {
        /**
         * Constructs this dummy token using the given position.
         *
         * @param position the position of this dummy token
         */
        public StartToken(final StreamPosition position) {
            super(position, null, null, position);
        }
    }
}
