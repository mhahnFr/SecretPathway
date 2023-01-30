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
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

/**
 * This class parses LPC source code.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class Parser {
    /** The tokenizer used by this parser. */
    private final Tokenizer tokenizer;

    /**
     * Constructs this parser using the given source.
     *
     * @param source the source code to be parsed
     */
    public Parser(final String source) {
        tokenizer = new Tokenizer(new StringStream(source));
    }

    /**
     * Peeks the next token.
     *
     * @return the peeked token
     */
    private Token peekToken() {
        final var token = tokenizer.nextToken();
        tokenizer.pushback(token);
        return token;
    }

    /**
     * Returns whether the next token's type is equal
     * to the given one. In that case, the token is consumed,
     * otherwise, it is pushed back.
     *
     * @param type the type to be checked
     * @return whether the next token has the given type
     */
    private boolean peekToken(final TokenType type) {
        final var token = tokenizer.nextToken();
        if (token.type() == type) {
            return true;
        }
        tokenizer.pushback(token);
        return false;
    }

    /**
     * Parses an {@code #include "something"} statement.
     *
     * @param previous the previous token
     * @return the parsed expression
     */
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

    /**
     * Parses an {@code inherit "maybe";} statement.
     *
     * @param previous the previous token
     * @return the parsed expression
     */
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

    /**
     * Parses the following modifiers.
     *
     * @return a collection with the read modifiers
     */
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

    private ASTExpression expect(final TokenType type, final Token token, final Token previous, final TokenType... next) {
        if (token.type() != type) {
            if (Arrays.asList(next).contains(token.type())) {
                tokenizer.pushback(token);
                return new ASTMissing(previous.endPos(), token.beginPos(), "Expected " + type + ", missing");
            } else {
                return new ASTWrong(token.beginPos(), token.endPos(), "Expected " + type + ", got " + token.type());
            }
        }
        return null;
    }

    private TokenType[] getBlockBeginTypes() {
        return new TokenType[] {
                TokenType.LET /* ... and much more ... */
        };
    }

    private ASTExpression[] parseBlock(final Token previous) {
        // TODO: Implement
        return null;
    }

    private ASTExpression parseFunctionDefinition(final Collection<ASTExpression> parts,
                                                  final Collection<TokenType>     modifiers,
                                                  final TokenType                 returnType,
                                                  final String                    name,
                                                  Token                           previous) {
        // TODO: varargs
        final var params = new Vector<ASTExpression>();
        Token token;
        while ((token = tokenizer.nextToken()).type() != TokenType.RIGHT_PAREN && token.type() != TokenType.EOF) {
            final var paramParts = new Vector<ASTExpression>(2);
            token = tokenizer.nextToken();
            final TokenType type;
            if (!isType(token)) {
                if (token.type() == TokenType.IDENTIFIER) {
                    tokenizer.pushback(token);
                    paramParts.add(new ASTMissing(previous.endPos(), token.beginPos(), "Expected a type"));
                } else {
                    paramParts.add(new ASTWrong(token.beginPos(), token.endPos(), "Expected a type"));
                }
                type = null;
            } else {
                type = token.type();
            }
            final var nextToken = tokenizer.nextToken();
            final var part = expect(TokenType.IDENTIFIER, nextToken, token, TokenType.COMMA, TokenType.RIGHT_PAREN);
            if (part != null) paramParts.add(part);
            if (!paramParts.isEmpty()) {
                final var paramPartsArray = new ASTExpression[paramParts.size() + 1];
                paramPartsArray[0] = new ASTParameter(token.beginPos(), nextToken.endPos(), type, null);
                System.arraycopy(paramParts.toArray(new ASTExpression[0]), 0, paramPartsArray, 1, paramParts.size());
                params.add(new ASTCombination(paramPartsArray));
            } else {
                params.add(new ASTParameter(token.beginPos(), nextToken.endPos(), type, (String) nextToken.payload()));
            }
            previous = nextToken;
        }
        final var block = parseBlock(previous);
        if (!parts.isEmpty()) {
            final var combination = new ASTExpression[params.size() + 1];

            combination[0] = new ASTFunctionDefinition(null, null, returnType, name, modifiers.toArray(new TokenType[0]), params.toArray(new ASTExpression[0]), block);
            System.arraycopy(params.toArray(new ASTExpression[0]), 0, combination, 1, params.size());
            return new ASTCombination(combination);
        }
        return new ASTFunctionDefinition(previous.beginPos(), block[block.length - 1].getEnd(), returnType, name, modifiers.toArray(new TokenType[0]), params.toArray(new ASTExpression[0]), block);
    }

    private ASTExpression parseVariableDefinition(final Collection<ASTExpression> parts,
                                                  final Collection<TokenType> modifiers,
                                                  final TokenType variableType,
                                                  final String name) {
        // TODO: Implement
        return null;
    }

    /**
     * Returns whether the given {@link Token} represents
     * a type.
     *
     * @param token the token to be checked
     * @return whether the token represents a type keyword
     */
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
            return parseFunctionDefinition(temps, modifiers, realType, name, id);
        } else if (t.type() == TokenType.SEMICOLON || t.type() == TokenType.EQUALS) {
            return parseVariableDefinition(temps, modifiers, realType, name);
        }
        // TODO: Continue until something known is reached
        throw new RuntimeException("Expected expression!");
    }

    /**
     * Parses the whole text. Returns a collection
     * with the parsed expressions.
     *
     * @return the parsed expressions
     */
    public Collection<ASTExpression> parse() {
        final var list = new ArrayList<ASTExpression>();

        while (!peekToken(TokenType.EOF)) {
            list.add(parseExpression());
        }

        return list;
    }
}
