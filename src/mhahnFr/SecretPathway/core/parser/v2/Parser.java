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

import mhahnFr.SecretPathway.core.parser.ast.ASTExpression;
import mhahnFr.SecretPathway.core.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.parser.tokenizer.Tokenizer;
import mhahnFr.utils.StreamPosition;
import mhahnFr.utils.StringStream;

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

    public ASTExpression[] parse() {
        return null;
    }

    private static class StartToken extends Token {
        public StartToken(final StreamPosition position) {
            super(position, null, null, position);
        }
    }
}
