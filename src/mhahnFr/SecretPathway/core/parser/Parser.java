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

    private ASTExpression parseExpression() {
        final var token = tokenizer.nextToken();
        if (token.type() == TokenType.INCLUDE) {
            return parseInclude(token);
        } else if (token.type() == TokenType.CLASS) {
            return parseClass(token);
        }
        tokenizer.pushback(token);
        return null; // ...
    }

    public Collection<ASTExpression> parse() {
        final var list = new ArrayList<ASTExpression>();

        while (!peekToken(TokenType.EOF)) {
            list.add(parseExpression());
        }

        return list;
    }
}
