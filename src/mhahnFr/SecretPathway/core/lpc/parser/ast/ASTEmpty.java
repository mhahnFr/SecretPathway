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
 * This class represents an empty statement as an AST node.
 *
 * @author mhahnFr
 * @since 14.02.23
 */
public class ASTEmpty extends ASTExpression {
    /**
     * Constructs this AST node using the given positions.
     *
     * @param begin the beginning position
     * @param end   the end position
     */
    public ASTEmpty(final StreamPosition begin,
                    final StreamPosition end) {
        super(begin, end, ASTType.EMPTY);
    }
}
