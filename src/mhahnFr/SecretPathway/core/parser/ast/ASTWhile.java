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

public class ASTWhile extends ASTExpression {
    private final ASTExpression condition;
    private final ASTExpression body;

    public ASTWhile(final StreamPosition begin,
                    final ASTExpression  condition,
                    final ASTExpression  body) {
        super(begin, body.getEnd(), ASTType.WHILE);

        this.condition = condition;
        this.body      = body;
    }

    public ASTExpression getCondition() {
        return condition;
    }

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
}
