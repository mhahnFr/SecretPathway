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
 * This class represents a {@code try catch} statement as an AST node.
 *
 * @author mhahnFr
 * @since 14.02.23
 */
public class ASTTryCatch extends ASTExpression {
    /** The try expression.              */
    private final ASTExpression tryExpression;
    /** The catching expression.         */
    private final ASTExpression catchExpression;
    /** The optional exception variable. */
    private final ASTExpression exceptionVariable;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin             the beginning position
     * @param tryExpression     the try expression
     * @param catchExpression   the catch expression
     * @param exceptionVariable the optional exception variable
     */
    public ASTTryCatch(final StreamPosition begin,
                       final ASTExpression  tryExpression,
                       final ASTExpression  catchExpression,
                       final ASTExpression  exceptionVariable) {
        super(begin, catchExpression.getEnd(), ASTType.TRY_CATCH);

        this.tryExpression     = tryExpression;
        this.catchExpression   = catchExpression;
        this.exceptionVariable = exceptionVariable;
    }

    /**
     * Returns the expression whose thrown exceptions are caught.
     *
     * @return the expression to be tried
     */
    public ASTExpression getTryExpression() {
        return tryExpression;
    }

    /**
     * Returns the expression handling the exception.
     *
     * @return the exception handling expression
     */
    public ASTExpression getCatchExpression() {
        return catchExpression;
    }

    /**
     * Returns the optional exception variable.
     *
     * @return the optional exception variable
     */
    public ASTExpression getExceptionVariable() {
        return exceptionVariable;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            tryExpression.visit(visitor);
            if (exceptionVariable != null) {
                exceptionVariable.visit(visitor);
            }
            catchExpression.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        final var indent = " ".repeat(Math.max(0, indentation));

        return super.describe(indentation) + " Try:\n" +
                tryExpression.describe(indentation + 4) + "\n" +
                (exceptionVariable == null ? "" : indent + "Catching:\n" +
                                                  exceptionVariable.describe(indentation + 4) + "\n") +
                indent + "Caught:\n" +
                catchExpression.describe(indentation + 4);
    }
}
