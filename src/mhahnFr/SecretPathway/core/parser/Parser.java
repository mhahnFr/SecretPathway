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

package mhahnFr.SecretPathway.core.parser;

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
 * @since 26.01.23
 */
public class Parser {
    private final Tokenizer tokenizer;

    public Parser(final String source) {
        tokenizer = new Tokenizer(new StringStream(source));
    }

    private Token peekToken() {
        final var token = tokenizer.nextToken();
        tokenizer.pushback(token);
        return token;
    }

    private boolean peekToken(final TokenType type) {
        final var token = tokenizer.nextToken();
        if (token.type() == type) {
            return true;
        }
        tokenizer.pushback(token);
        return false;
    }

    private ASTExpression parseInclude(final Token previous) {
        final var token = tokenizer.nextToken();

        if (token.type() != TokenType.STRING) {
            final ASTMissing missing;

            if (previous.endPos().isOnSameLine(token.beginPos())) {
               tokenizer.pushback(token);
                missing = new ASTWrong(token.beginPos(), token.endPos(), "Expected a string");
            } else {
                missing = new ASTMissing(previous.endPos(), token.beginPos(), "Declare file to be included");
            }

            return new ASTCombination(new ASTInclude(previous.beginPos(), token.endPos(), null),
                                      missing);
        }
        return new ASTInclude(previous.beginPos(), token.endPos(), (String) token.payload());
    }

    private ASTExpression parseClass(final Token previous) {
        return null;
    }

    private ASTExpression parseInherit(final Token previous) {
        var token = tokenizer.nextToken();

        StreamPosition lastPos = previous.endPos();
        String inherited = null;
        if (token.type() == TokenType.STRING) {
            inherited = (String) token.payload();
            lastPos = token.endPos();
            token = tokenizer.nextToken();
        }
        if (token.type() != TokenType.SEMICOLON) {
            tokenizer.pushback(token);
            return new ASTCombination(new ASTInheritance(previous.beginPos(), lastPos, inherited),
                                      new ASTMissing(lastPos, token.beginPos(), "Expected semicolon"));
        }
        return new ASTInheritance(previous.beginPos(), token.beginPos(), inherited);
    }

    private Collection<TokenType> parseModifiers() {
        final var toReturn = new Vector<TokenType>();

        while (true) {
            final var token = tokenizer.nextToken();
            final var type  = token.type();
            if (type == TokenType.PRIVATE ||
                type == TokenType.PROTECTED ||
                type == TokenType.PUBLIC ||
                type == TokenType.DEPRECATED ||
                type == TokenType.OVERRIDE) {
                toReturn.add(type);
            } else {
                tokenizer.pushback(token);
                break;
            }
        }

        return toReturn;
    }

    private ASTExpression parseFunctionDefinition(final Collection<ASTExpression> parts,
                                                  final Collection<TokenType> modifiers,
                                                  final TokenType returnType,
                                                  final String name) {
        // TODO: Implement
        return null;
    }

    private ASTExpression parseVariableDefinition(final Collection<ASTExpression> parts,
                                                  final Collection<TokenType> modifiers,
                                                  final TokenType variableType,
                                                  final String name) {
        // TODO: Implement
        return null;
    }

    private boolean isType(final Token token) {
        final var type = token.type();

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

    private ASTExpression parseExpression() {
        final var token = tokenizer.nextToken();
        if (token.type() == TokenType.INHERIT) {
            return parseInherit(token);
        } else if (token.type() == TokenType.INCLUDE) {
            return parseInclude(token);
        } else if (token.type() == TokenType.CLASS) {
            return parseClass(token);
        }
        tokenizer.pushback(token);
        final var modifiers = parseModifiers();
        final var temps = new Vector<ASTExpression>();
        final var type = tokenizer.nextToken();
        final TokenType realType;
        if (!isType(type)) {
            temps.add(new ASTMissing(type.beginPos(), type.beginPos(), "Expected a type"));
            tokenizer.pushback(type);
            realType = null;
        } else {
            realType = type.type();
        }
        final String name;
        final var id = tokenizer.nextToken();
        if (id.type() != TokenType.IDENTIFIER) {
            name = null;
            final ASTExpression fill;
            if (id.type() == TokenType.LEFT_BRACKET || id.type() == TokenType.SEMICOLON || id.type() == TokenType.EQUALS) {
                fill = new ASTMissing(type.endPos(), id.beginPos(), "Expected name of identifier");
                tokenizer.pushback(id);
            } else {
                fill = new ASTWrong(id.beginPos(), id.endPos(), "Expected name of identifier");
            }
            temps.add(fill);
        } else {
            name = (String) id.payload();
        }
        var t = tokenizer.nextToken();
        if (t.type() == TokenType.LEFT_BRACKET) {
            return parseFunctionDefinition(temps, modifiers, realType, name);
        } else if (t.type() == TokenType.SEMICOLON || t.type() == TokenType.EQUALS) {
            return parseVariableDefinition(temps, modifiers, realType, name);
        }
        // TODO: Continue until something known is reached
        throw new RuntimeException("Expected expression!");
    }

    public Collection<ASTExpression> parse() {
        final var list = new ArrayList<ASTExpression>();

        while (!peekToken(TokenType.EOF)) {
            list.add(parseExpression());
        }

        return list;
    }
}
