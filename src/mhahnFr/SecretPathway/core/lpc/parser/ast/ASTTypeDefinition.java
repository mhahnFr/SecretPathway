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
 * This class consolidates type declarations.
 *
 * @author mhahnFr
 * @since 04.03.23
 */
public abstract class ASTTypeDefinition extends ASTExpression {
    /**
     * Constructs this expression using the given positions.
     *
     * @param begin the beginning position
     * @param end   the end position
     * @param type  the type of this AST node
     */
    protected ASTTypeDefinition(StreamPosition begin, StreamPosition end, ASTType type) {
        super(begin, end, type);
    }

    /**
     * Returns whether the type represented by this instance can be assigned
     * by the given other {@link ASTTypeDefinition}.
     *
     * @param other the other type representation
     * @return whether the given type can be assigned to this type
     */
    public abstract boolean isAssignableFrom(final ASTTypeDefinition other);
}
