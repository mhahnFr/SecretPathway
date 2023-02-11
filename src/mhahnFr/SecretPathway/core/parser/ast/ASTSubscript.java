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
 * This class represents a subscript expression as an AST node.
 *
 * @author mhahnFr
 * @since 10.20.23
 */
public class ASTSubscript extends ASTExpression {
    /** The underlying expression. */
    private final ASTExpression expression;

    /**
     * Constructs this AST node using the given expression.
     *
     * @param expression the expression to be represented as subscript
     */
    public ASTSubscript(final ASTExpression expression) {
        super(expression.getBegin(), expression.getEnd(), ASTType.SUBSCRIPT);

        this.expression = expression;
    }

    /**
     * Returns the underlying expression
     *
     * @return the underlying expression
     */
    public ASTExpression getExpression() {
        return expression;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            expression.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + "\n" + expression.describe(indentation + 4);
    }
}
