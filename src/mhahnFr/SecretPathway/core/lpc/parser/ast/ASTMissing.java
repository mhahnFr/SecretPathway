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

import mhahnFr.utils.StreamPosition;

/**
 * This class represents a missing statement in the AST.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class ASTMissing extends ASTExpression {
    /** The message about the missing node. */
    private final String message;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin   the beginning position
     * @param end     the end position
     * @param message the message what node is missing
     */
    public ASTMissing(final StreamPosition begin,
                      final StreamPosition end,
                      final String         message) {
        super(begin, end, ASTType.MISSING);

        this.message = message;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " " + message;
    }
}
