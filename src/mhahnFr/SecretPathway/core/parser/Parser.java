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

import mhahnFr.SecretPathway.core.parser.ast.ASTExpression;
import mhahnFr.SecretPathway.core.parser.ast.ASTInclude;
import mhahnFr.SecretPathway.core.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.parser.tokenizer.Tokenizer;
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

    private ASTInclude parseInclude() {
        return null;
    }

    private ASTExpression parseClass() {
        return null;
    }

    private ASTExpression parseExpression() {
        if (peekToken(TokenType.INCLUDE)) {
            return parseInclude();
        } else if (peekToken(TokenType.CLASS)) {
            return parseClass();
        }
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
