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
 * This class represents a return statement as an AST node.
 *
 * @author mhahnFr
 * @since 04.02.23
 */
public class ASTReturn extends ASTExpression {
    /** The returned expression. */
    private final ASTExpression returned;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin    the beginning of this expression
     * @param returned the returned expression
     * @param end      the end of this expression
     */
    public ASTReturn(final StreamPosition begin,
                     final ASTExpression  returned,
                     final StreamPosition end) {
        super(begin, end, ASTType.RETURN);

        this.returned = returned;
    }

    /**
     * Returns the returned expression.
     *
     * @return the returned expression
     */
    public ASTExpression getReturned() {
        return returned;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this) && returned != null) {
            returned.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + "\n" + (returned == null ? " ".repeat(Math.max(0, indentation + 4)) : returned.describe(indentation + 4));
    }
}
