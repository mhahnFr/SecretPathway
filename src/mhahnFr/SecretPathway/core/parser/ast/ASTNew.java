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
 * This class represents a new expression as an AST node.
 *
 * @author mhahnFr
 * @since 08.02.23
 */
public class ASTNew extends ASTExpression {
    /** The instancing expression. */
    private final ASTExpression instancingExpression;
    /** The argument expressions.  */
    private final ASTExpression[] arguments;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin                the beginning position
     * @param end                  the end position
     * @param instancingExpression the instancing expression
     * @param arguments            the argument expressions
     */
    public ASTNew(final StreamPosition  begin,
                  final StreamPosition  end,
                  final ASTExpression   instancingExpression,
                  final ASTExpression[] arguments) {
        super(begin, end, ASTType.NEW);

        this.instancingExpression = instancingExpression;
        this.arguments            = arguments;
    }

    /**
     * Returns the instancing expression.
     *
     * @return the instancing expression
     */
    public ASTExpression getInstancingExpression() {
        return instancingExpression;
    }

    /**
     * Returns the argument expressions.
     *
     * @return the argument expressions
     */
    public ASTExpression[] getArguments() {
        return arguments;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            instancingExpression.visit(visitor);

            if (arguments != null) {
                for (int i = 0; i < arguments.length; ++i) {
                    arguments[i].visit(visitor);
                }
            }
        }
    }
}
