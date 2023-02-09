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
 * This class represents a cast as an AST node.
 *
 * @author mhahnFr
 * @since 08.02.23
 */
public class ASTCast extends ASTExpression {
    /** The type to which to cast. */
    private final ASTExpression type;
    /** The cast expression.       */
    private final ASTExpression cast;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin the beginning position of this expression
     * @param type  the type to which to cast
     * @param cast  the expression to be cast
     */
    public ASTCast(final StreamPosition begin,
                   final ASTExpression  type,
                   final ASTExpression  cast) {
        super(begin, cast.getEnd(), ASTType.CAST);

        this.type = type;
        this.cast = cast;
    }

    /**
     * Returns the type to which the expression should be
     * cast to.
     *
     * @return the casting type
     */
    public ASTExpression getType() {
        return type;
    }

    /**
     * Returns the expression to be cast.
     *
     * @return the cast expression
     */
    public ASTExpression getCast() {
        return cast;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            type.visit(visitor);
            cast.visit(visitor);
        }
    }
}
