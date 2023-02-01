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
import mhahnFr.utils.Pair;
import mhahnFr.utils.StreamPosition;
import mhahnFr.utils.StringStream;

import java.util.*;

/**
 * This class parses LPC source code.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class Parser {
    /** The tokenizer used by this parser. */
    private final Tokenizer tokenizer;
    private Token previous;

    /**
     * Constructs this parser using the given source.
     *
     * @param source the source code to be parsed
     */
    public Parser(final String source) {
        tokenizer = new Tokenizer(new StringStream(source));
        previous  = peekToken();
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
     * @return the parsed expression
     */
    private ASTExpression parseInclude() {
        final var token = tokenizer.nextToken();

        if (token.type() != TokenType.STRING) {
            final ASTExpression missing;

            if (previous.endPos().isOnSameLine(token.beginPos())) {
               tokenizer.pushback(token);
                missing = new ASTWrong(token, "Expected a string");
            } else {
                missing = new ASTMissing(previous.endPos(), token.beginPos(), "Declare file to be included");
            }

            return new ASTCombination(new ASTInclude(previous.beginPos(), token.endPos(), null),
                                      missing);
        }
        return new ASTInclude(previous.beginPos(), token.endPos(), (String) token.payload());
    }

    private ASTExpression parseClass() {
        return null;
    }

    /**
     * Parses an {@code inherit "maybe";} statement.
     *
     * @return the parsed expression
     */
    private ASTExpression parseInherit() {
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
    private Pair<Collection<TokenType>, StreamPosition> parseModifiers() {
        final var toReturn = new Vector<TokenType>();
        StreamPosition lastEndPos = previous.endPos();

        while (true) {
            final var token = tokenizer.nextToken();
            final var type  = token.type();
            if (type == TokenType.PRIVATE ||
                type == TokenType.PROTECTED ||
                type == TokenType.PUBLIC ||
                type == TokenType.DEPRECATED ||
                type == TokenType.OVERRIDE) {
                toReturn.add(type);
                lastEndPos = token.endPos();
            } else {
                tokenizer.pushback(token);
                break;
            }
        }

        return new Pair<>(toReturn, lastEndPos);
    }

    private ASTExpression expect(final TokenType type, final Token token, final TokenType... next) {
        if (token.type() != type) {
            if (Arrays.asList(next).contains(token.type())) {
                tokenizer.pushback(token);
                return new ASTMissing(previous.endPos(), token.beginPos(), "Expected " + type + ", missing");
            } else {
                return new ASTWrong(token, "Expected " + type + ", got " + token.type());
            }
        }
        return null;
    }

    private TokenType[] getBlockBeginTypes() {
        return new TokenType[] {
                TokenType.LET /* ... and much more ... */
        };
    }

    private ASTExpression[] parseBlock() {
        // TODO: Implement
        return null;
    }

    private ASTExpression parseFunctionDefinition(final Collection<ASTExpression> parts,
                                                  final Collection<TokenType>     modifiers,
                                                  final TokenType                 returnType,
                                                  final String                    name) {
        final var params = new Vector<ASTExpression>();
        Token token;
        var stop = false;
        while ((token = tokenizer.nextToken()).type() != TokenType.RIGHT_PAREN && token.type() != TokenType.EOF && !stop) {
            final var paramParts = new Vector<ASTExpression>(3);
            final TokenType type;
            if (token.type() == TokenType.ELLIPSIS) {
                final var nextToken = tokenizer.nextToken();
                if (nextToken.type() == TokenType.LEFT_CURLY) {
                    parts.add(new ASTMissing(token.endPos(), nextToken.beginPos(), "Expected ')'"));
                    params.add(new ASTEllipsis(token));
                    tokenizer.pushback(nextToken);
                    break;
                } else if (nextToken.type() != TokenType.RIGHT_PAREN) {
                    final var peeked = peekToken();
                    if (peeked.type() == TokenType.LEFT_CURLY) {
                        parts.add(new ASTWrong(nextToken, "Expected ')'"));
                        params.add(new ASTEllipsis(token));
                        break;
                    } else {
                        tokenizer.pushback(nextToken);
                    }
                } else {
                    params.add(new ASTEllipsis(token));
                    break;
                }
            }
            if (!isType(token)) {
                if (token.type() == TokenType.IDENTIFIER && peekToken().type() != TokenType.IDENTIFIER) {
                    tokenizer.pushback(token);
                    paramParts.add(new ASTMissing(previous.endPos(), token.beginPos(), "Expected a type"));
                } else {
                    paramParts.add(new ASTWrong(token, "Expected a type"));
                }
                type = null;
            } else {
                type = token.type();
            }

            final var nextToken = tokenizer.nextToken();
            previous = token;
            final var part = expect(TokenType.IDENTIFIER, nextToken, TokenType.COMMA, TokenType.RIGHT_PAREN);
            final String paramName;
            if (part != null) {
                paramParts.add(part);
                paramName = null;
            } else {
                paramName = (String) nextToken.payload();
            }

            token = tokenizer.nextToken();
            if (token.type() != TokenType.COMMA && token.type() != TokenType.RIGHT_PAREN) {
                if (token.type() == TokenType.LEFT_CURLY) {
                    tokenizer.pushback(token);
                    stop = true;
                    parts.add(new ASTMissing(nextToken.endPos(), token.beginPos(), "Expected ')'"));
                } else if (isType(token)) {
                    tokenizer.pushback(token);
                    parts.add(new ASTMissing(nextToken.endPos(), token.beginPos(), "Expected ','"));
                } else {
                    parts.add(new ASTWrong(token, "Unexpected token"));
                }
            } else if (token.type() == TokenType.RIGHT_PAREN) {
                tokenizer.pushback(token);
            } else {
                final var peeked = peekToken();
                if (peeked.type() == TokenType.RIGHT_PAREN || peeked.type() == TokenType.COMMA) {
                    params.add(new ASTMissing(token.endPos(), peeked.beginPos(), "Expected parameter"));
                    if (peeked.type() == TokenType.COMMA) {
                        tokenizer.nextToken();
                    }
                }
            }

            if (!paramParts.isEmpty()) {
                final var paramPartsArray = new ASTExpression[paramParts.size() + 1];
                paramPartsArray[0] = new ASTParameter(token.beginPos(), nextToken.endPos(), type, paramName);
                System.arraycopy(paramParts.toArray(new ASTExpression[0]), 0, paramPartsArray, 1, paramParts.size());
                params.add(new ASTCombination(paramPartsArray));
            } else {
                params.add(new ASTParameter(token.beginPos(), nextToken.endPos(), type, paramName));
            }
            previous = token;
        }
        final var block = parseBlock();
        if (!parts.isEmpty()) {
            final var combination = new ASTExpression[parts.size() + 1];

            combination[0] = new ASTFunctionDefinition(null, null, returnType, name, modifiers.toArray(new TokenType[0]), params.toArray(new ASTExpression[0]), block);
            System.arraycopy(parts.toArray(new ASTExpression[0]), 0, combination, 1, parts.size());
            return new ASTCombination(combination);
        }
        return new ASTFunctionDefinition(previous.beginPos(), /*block[block.length - 1].getEnd()*/null, returnType, name, modifiers.toArray(new TokenType[0]), params.toArray(new ASTExpression[0]), block);
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
        previous = token;
        if (token.type() == TokenType.INHERIT) {
            return parseInherit();
        } else if (token.type() == TokenType.INCLUDE) {
            return parseInclude();
        } else if (token.type() == TokenType.CLASS) {
            return parseClass();
        }
        tokenizer.pushback(token);
        final var modifiers = parseModifiers();
        final var temps = new Vector<ASTExpression>();
        final var type = tokenizer.nextToken();
        final TokenType realType;
        if (!isType(type)) {
            if (peekToken().type() == TokenType.IDENTIFIER) {
                temps.add(new ASTWrong(type, "Expected a type"));
            } else {
                temps.add(new ASTMissing(modifiers.getSecond(), type.beginPos(), "Expected a type"));
                tokenizer.pushback(type);
            }
            realType = null;
        } else {
            realType = type.type();
        }
        final String name;
        final var id = tokenizer.nextToken();
        if (id.type() != TokenType.IDENTIFIER) {
            name = null;
            final ASTExpression fill;
            if (id.type() == TokenType.LEFT_PAREN || id.type() == TokenType.SEMICOLON || id.type() == TokenType.EQUALS) {
                fill = new ASTMissing(type.endPos(), id.beginPos(), "Expected name of identifier");
                tokenizer.pushback(id);
            } else {
                fill = new ASTWrong(id, "Expected name of identifier");
            }
            temps.add(fill);
        } else {
            name = (String) id.payload();
        }
        var t = tokenizer.nextToken();
        previous = t;
        if (t.type() == TokenType.LEFT_PAREN) {
            return parseFunctionDefinition(temps, modifiers.getFirst(), realType, name);
        } else if (t.type() == TokenType.SEMICOLON || t.type() == TokenType.EQUALS) {
            return parseVariableDefinition(temps, modifiers.getFirst(), realType, name);
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

        try {
            while (!peekToken(TokenType.EOF)) {
                list.add(parseExpression());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return list;
    }
}
