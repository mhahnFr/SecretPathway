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

package mhahnFr.SecretPathway.core.parser.ast;

import mhahnFr.utils.StreamPosition;

/**
 * This class represents a node in the AST.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public abstract class ASTExpression {
    /** The beginning position of this expression. */
    private final StreamPosition begin;
    /** The end position of this expression.       */
    private final StreamPosition end;

    /**
     * Constructs this expression using the given positions.
     *
     * @param begin the beginning position
     * @param end the end position
     */
    protected ASTExpression(StreamPosition begin, StreamPosition end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Returns the end position of this expression.
     *
     * @return the end position
     */
    public StreamPosition getEnd() {
        return end;
    }

    /**
     * Returns the beginning position of this expression.
     *
     * @return the beginning position
     */
    public StreamPosition getBegin() {
        return begin;
    }
}
