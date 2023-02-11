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
 * This class represents a function call as an AST node.
 *
 * @author mhahnFr
 * @since 08.02.23
 */
public class ASTFunctionCall extends ASTExpression {
    /** The name of the called function.                       */
    private final ASTExpression name;
    /** The expressions whose result are passed as parameters. */
    private final ASTExpression[] arguments;

    /**
     * Constructs this function call AST node using the given
     * name, arguments and the given end position.
     *
     * @param name      the name
     * @param arguments the argument expressions
     * @param end       the end position
     */
    public ASTFunctionCall(final ASTExpression   name,
                           final ASTExpression[] arguments,
                           final StreamPosition  end) {
        super(name.getBegin(), end, ASTType.FUNCTION_CALL);

        this.name      = name;
        this.arguments = arguments;
    }

    /**
     * Returns the name of the called function.
     *
     * @return the name
     */
    public ASTExpression getName() {
        return name;
    }

    /**
     * Returns the argument expressions of this function call.
     *
     * @return the argument expressions
     */
    public ASTExpression[] getArguments() {
        return arguments;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            name.visit(visitor);

            if (arguments != null) {
                for (int i = 0; i < arguments.length; ++i) {
                    arguments[i].visit(visitor);
                }
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" function name:\n");
        builder.append(name.describe(indentation + 4)).append('\n');

        if (arguments != null) {
            builder.append(" ".repeat(Math.max(0, indentation))).append("arguments:\n");
            for (int i = 0; i < arguments.length; ++i) {
                builder.append(arguments[i].describe(indentation + 4));
                if (i + 1 < arguments.length) {
                    builder.append('\n');
                }
            }
        }

        return builder.toString();
    }
}
