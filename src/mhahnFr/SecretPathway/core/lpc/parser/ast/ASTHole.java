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
import mhahnFr.utils.StreamPosition;

/**
 * This class represents a hole as an AST node.
 *
 * @author mhahnFr
 * @since 05.04.23
 */
public abstract class ASTHole extends ASTExpression {
    /** The message.               */
    private final String message;
    /** The instead expected type. */
    private final HighlightType expected;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin    the beginning position
     * @param end      the end position
     * @param message  the message
     * @param self     the type of the overriding class
     * @param expected the instead expected type
     */
    protected ASTHole(final StreamPosition     begin,
                      final StreamPosition     end,
                      final String             message,
                      final ASTType            self,
                      final HighlightType expected) {
        super(begin, end, self);

        this.message  = message;
        this.expected = expected;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the instead expected type.
     *
     * @return the expected type
     */
    public HighlightType getExpected() {
        return expected;
    }
}
