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
 * This class represents a {@code foreach} statement as an AST node.
 *
 * @author mhahnFr
 * @since 14.02.23
 */
public class ASTForEach extends ASTExpression {
    /** The variable expression. */
    private final ASTExpression variable;
    /** The range expression.    */
    private final ASTExpression rangeExpression;
    /** The body.                */
    private final ASTExpression body;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin           the beginning position
     * @param variable        the variable declaration
     * @param rangeExpression the range expression extracting the variable
     * @param body            the body
     */
    public ASTForEach(final StreamPosition begin,
                      final ASTExpression  variable,
                      final ASTExpression  rangeExpression,
                      final ASTExpression  body) {
        super(begin, body.getEnd(), ASTType.AST_FOREACH);

        this.variable        = variable;
        this.rangeExpression = rangeExpression;
        this.body            = body;
    }

    /**
     * Returns the variable declaration.
     *
     * @return the variable declaration
     */
    public ASTExpression getVariable() {
        return variable;
    }

    /**
     * Returns the range expression that should extract the
     * declared variable.
     *
     * @return the range expression
     */
    public ASTExpression getRangeExpression() {
        return rangeExpression;
    }

    /**
     * Returns the body of this loop.
     *
     * @return the body
     */
    public ASTExpression getBody() {
        return body;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            variable.visit(visitor);
            rangeExpression.visit(visitor);
            body.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        final var indent = " ".repeat(Math.max(0, indentation));

        return super.describe(indentation) + " Variable:\n" +
                variable.describe(indentation + 4) + "\n" +
                indent + "Range expression:\n" +
                rangeExpression.describe(indentation + 4) + "\n" +
                indent + "Body:\n" +
                body.describe(indentation + 4);
    }
}
