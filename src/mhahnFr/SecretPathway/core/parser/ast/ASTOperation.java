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

/**
 * This class represents an operation as an AST node.
 *
 * @author mhahnFr
 * @since 07.02.23
 */
public class ASTOperation extends ASTExpression {
    /** The left hand side expression.  */
    private final ASTExpression lhs;
    /** The right hand side expression. */
    private final ASTExpression rhs;

    /**
     * Constructs this AST node using the two given sub-expressions.
     *
     * @param lhs the left hand side expression
     * @param rhs the right hand side expression
     */
    public ASTOperation(final ASTExpression lhs,
                        final ASTExpression rhs) {
        super(lhs.getBegin(), rhs.getEnd(), ASTType.OPERATION);

        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Returns the left hand side expression of this operation.
     *
     * @return the left hand side expression
     */
    public ASTExpression getLhs() {
        return lhs;
    }

    /**
     * Returns the right hand side expression of this operation.
     *
     * @return the right hand side expression
     */
    public ASTExpression getRhs() {
        return rhs;
    }
}
