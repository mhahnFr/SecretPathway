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
 * This class represents a switch case statement as an AST node.
 *
 * @author mhahnFr
 * @since 15.02.23
 */
public class ASTCase extends ASTExpression {
    /** The actual case expression.                */
    private final ASTExpression caseStatement;
    /** The expressions associated with this case. */
    private final ASTExpression[] expressions;

    /**
     * Constructs this AST node using the given information.
     *
     * @param caseStatement the actual case statement
     * @param expressions   the associated expressions
     */
    public ASTCase(final ASTExpression   caseStatement,
                   final ASTExpression[] expressions) {
        super(caseStatement.getBegin(),
                (expressions.length == 0 ? caseStatement : expressions[expressions.length - 1]).getEnd(),
                ASTType.CASE);

        this.caseStatement = caseStatement;
        this.expressions   = expressions;
    }

    /**
     * Returns the actual case statement.
     *
     * @return the case statement
     */
    public ASTExpression getCaseStatement() {
        return caseStatement;
    }

    /**
     * Returns the expressions associated with this case statement.
     *
     * @return the associated expressions
     */
    public ASTExpression[] getExpressions() {
        return expressions;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            caseStatement.visit(visitor);

            for (int i = 0; i < expressions.length; ++i) {
                expressions[i].visit(visitor);
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" Case:\n")
                .append(caseStatement.describe(indentation + 4)).append('\n')
                .append(" ".repeat(Math.max(0, indentation))).append("Statements:\n");

        for (int i = 0; i < expressions.length; ++i) {
            builder.append(expressions[i].describe(indentation + 4));
            if (i + 1 < expressions.length) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
