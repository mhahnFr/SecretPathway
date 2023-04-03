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

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a {@code for} loop as an AST node.
 *
 * @author mhahnFr
 * @since 14.02.23
 */
public class ASTFor extends ASTExpression {
    /** The initial expression.     */
    private final ASTExpression initExpression;
    /** The conditional expression. */
    private final ASTExpression condition;
    /** The after expression.       */
    private final ASTExpression afterExpression;
    /** The body.                   */
    private final ASTExpression body;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin           the beginning position
     * @param initExpression  the initial expression
     * @param condition       the conditional expression
     * @param afterExpression the after expression
     * @param body            the body
     */
    public ASTFor(final StreamPosition begin,
                  final ASTExpression  initExpression,
                  final ASTExpression  condition,
                  final ASTExpression  afterExpression,
                  final ASTExpression  body) {
        super(begin, body.getEnd(), ASTType.AST_FOR);

        this.initExpression  = initExpression;
        this.condition       = condition;
        this.afterExpression = afterExpression;
        this.body            = body;
    }

    /**
     * Returns the initial loop expression.
     *
     * @return the initial expression
     */
    public ASTExpression getInitExpression() {
        return initExpression;
    }

    /**
     * Returns the conditional expression.
     *
     * @return the conditional expression
     */
    public ASTExpression getCondition() {
        return condition;
    }

    /**
     * Returns the after expression.
     *
     * @return the after expression
     */
    public ASTExpression getAfterExpression() {
        return afterExpression;
    }

    /**
     * Returns the body of the loop.
     *
     * @return the body
     */
    public ASTExpression getBody() {
        return body;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            initExpression.visit(visitor);
            condition.visit(visitor);
            afterExpression.visit(visitor);
            body.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        final var indent = " ".repeat(Math.max(0, indentation));

        return super.describe(indentation) + " Initial:\n" +
                initExpression.describe(indentation + 4) + "\n" +
                indent + "Condition:\n" +
                condition.describe(indentation + 4) + "\n" +
                indent + "After each loop:\n" +
                afterExpression.describe(indentation + 4) + "\n" +
                indent + "Body:\n" +
                body.describe(indentation + 4);
    }

    @Override
    public boolean hasSubExpressions() {
        return true;
    }

    @Override
    public List<ASTExpression> getSubExpressions() {
        return Arrays.asList(initExpression, condition, afterExpression, body);
    }
}
