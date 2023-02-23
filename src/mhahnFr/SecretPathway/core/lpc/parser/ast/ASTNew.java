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
 * This class represents a new expression as an AST node.
 *
 * @author mhahnFr
 * @since 08.02.23
 */
public class ASTNew extends ASTExpression {
    /** The instancing expression. */
    private final ASTExpression instancingExpression;
    /** The argument expressions.  */
    private final List<ASTExpression> arguments;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin                the beginning position
     * @param end                  the end position
     * @param instancingExpression the instancing expression
     * @param arguments            the argument expressions
     */
    public ASTNew(final StreamPosition      begin,
                  final StreamPosition      end,
                  final ASTExpression       instancingExpression,
                  final List<ASTExpression> arguments) {
        super(begin, end, ASTType.AST_NEW);

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
    public List<ASTExpression> getArguments() {
        return arguments;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            instancingExpression.visit(visitor);

            if (arguments != null) {
                final var iterator = arguments.listIterator();
                while (iterator.hasNext()) {
                    iterator.next().visit(visitor);
                }
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" what:\n")
               .append(instancingExpression.describe(indentation + 4)).append('\n');

        if (arguments != null) {
            builder.append(" ".repeat(Math.max(0, indentation))).append("arguments:\n");
            final var iterator = arguments.listIterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next().describe(indentation + 4));
                if (iterator.hasNext()) {
                    builder.append('\n');
                }
            }
        }

        return builder.toString();
    }
}
