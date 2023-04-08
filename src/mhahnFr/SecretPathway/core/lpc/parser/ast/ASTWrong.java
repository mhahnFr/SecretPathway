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

package mhahnFr.SecretPathway.core.lpc.parser.ast;

import mhahnFr.SecretPathway.core.lpc.interpreter.highlight.HighlightType;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Token;

/**
 * This class represents a wrong statement in the AST.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class ASTWrong extends ASTHole {
    /** The message about this wrong node. */
    private final String message;

    /**
     * Constructs this AST node using the given {@link Token}
     * and the given message.
     *
     * @param token    the represented token
     * @param message  the reason why the token is wrong
     * @param expected the instead expected AST type
     */
    public ASTWrong(final Token        token,
                    final String       message,
                    final HighlightType expected) {
        super(token.beginPos(), token.endPos(), message, ASTType.WRONG, expected);

        this.message = message;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " " + message;
    }
}
