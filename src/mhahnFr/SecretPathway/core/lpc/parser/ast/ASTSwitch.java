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

import java.util.List;

/**
 * This class represents a complete switch statement as an AST node.
 *
 * @author mhahnFr
 * @since 15.02.23
 */
public class ASTSwitch extends ASTExpression {
    /** The variable expression.            */
    private final ASTExpression variableExpression;
    /** The cases in this switch statement. */
    private final List<ASTExpression> cases;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin              the beginning position
     * @param end                the end position
     * @param variableExpression the variable expression
     * @param cases              the cases
     */
    public ASTSwitch(final StreamPosition      begin,
                     final StreamPosition      end,
                     final ASTExpression       variableExpression,
                     final List<ASTExpression> cases) {
        super(begin, end, ASTType.AST_SWITCH);

        this.variableExpression = variableExpression;
        this.cases              = cases;
    }

    /**
     * Returns the variable expression of this statement.
     *
     * @return the variable expression
     */
    public ASTExpression getVariableExpression() {
        return variableExpression;
    }

    /**
     * Returns the declared cases of this switch statement.
     *
     * @return the cases
     */
    public List<ASTExpression> getCases() {
        return cases;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            variableExpression.visit(visitor);

            final var iterator = cases.listIterator();
            while (iterator.hasNext()) {
                iterator.next().visit(visitor);
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" Variable:\n")
                .append(variableExpression.describe(indentation + 4)).append('\n');

        builder.append(" ".repeat(Math.max(0, indentation))).append("Cases:\n");
        final var iterator = cases.listIterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().describe(indentation + 4));
            if (iterator.hasNext()) {
                builder.append('\n');
            }
        }

        return builder.toString();
    }
}
