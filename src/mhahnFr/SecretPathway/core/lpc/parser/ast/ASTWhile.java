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
 * This class represents a {@code while} statement as an AST node.
 *
 * @author mhahnFr
 * @since 14.02.23
 */
public class ASTWhile extends ASTExpression {
    /** The condition expression. */
    private final ASTExpression condition;
    /** The loop's body.          */
    private final ASTExpression body;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin     the beginning position
     * @param condition the condition
     * @param body      the body
     * @param doWhile   whether this loop is a {@code do while} loop
     */
    public ASTWhile(final StreamPosition begin,
                    final ASTExpression  condition,
                    final ASTExpression  body,
                    final boolean        doWhile) {
        super(begin, body.getEnd(), doWhile ? ASTType.DO_WHILE : ASTType.AST_WHILE);

        this.condition = condition;
        this.body      = body;
    }

    /**
     * Returns the condition expression of this {@code while} statement.
     *
     * @return the condition
     */
    public ASTExpression getCondition() {
        return condition;
    }

    /**
     * Returns the body of this {@code while} statement.
     *
     * @return the body
     */
    public ASTExpression getBody() {
        return body;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            condition.visit(visitor);
            body.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " Condition:\n" +
                condition.describe(indentation + 4) + "\n" +
                " ".repeat(Math.max(0, indentation)) + "Body:\n" +
                body.describe(indentation + 4);
    }

    @Override
    public boolean hasSubExpressions() {
        return true;
    }

    @Override
    public List<ASTExpression> getSubExpressions() {
        return Arrays.asList(condition, body);
    }
}
