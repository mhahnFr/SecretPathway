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
 * This class represents a class definition as an AST node.
 *
 * @author mhahnFr
 * @since 15.02.23
 */
public class ASTClass extends ASTExpression {
    /** The name expression of this class.                */
    private final ASTExpression name;
    /** The inheritance expression of the shorthand form. */
    private final ASTExpression inheritance;
    /** The statements of the class.                      */
    private final ASTExpression[] statements;

    /**
     * Constructs this class using the inheritance expression
     * of the shorthand form.
     *
     * @param begin       the beginning position
     * @param name        the name expression
     * @param inheritance the inheritance expression
     */
    public ASTClass(final StreamPosition begin,
                    final ASTExpression  name,
                    final ASTExpression  inheritance) {
        super(begin, (inheritance == null ? name : inheritance).getEnd(), ASTType.CLASS);

        this.name        = name;
        this.inheritance = inheritance;
        this.statements  = null;
    }

    /**
     * Constructs this class using the statements of the traditional
     * body.
     *
     * @param begin      the beginning position
     * @param name       the name expression
     * @param statements the body
     */
    public ASTClass(final StreamPosition  begin,
                    final ASTExpression   name,
                    final ASTExpression[] statements) {
        super(begin, (statements.length == 0 ? name : statements[statements.length - 1]).getEnd(), ASTType.CLASS);

        this.name        = name;
        this.inheritance = null;
        this.statements  = statements;
    }

    /**
     * Returns the name expression of this class.
     *
     * @return the name expression
     */
    public ASTExpression getName() {
        return name;
    }

    /**
     * Returns the inheritance expression of the shorthand form.
     *
     * @return the inheritance expression
     */
    public ASTExpression getInheritance() {
        return inheritance;
    }

    /**
     * Returns the body statements of the traditional
     * class form.
     *
     * @return the body statements
     */
    public ASTExpression[] getStatements() {
        return statements;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            name.visit(visitor);
            if (inheritance != null) {
                inheritance.visit(visitor);
            }
            if (statements != null) {
                for (int i = 0; i < statements.length; ++i) {
                    statements[i].visit(visitor);
                }
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var indent  = " ".repeat(Math.max(0, indentation));
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" Name:\n")
                .append(name.describe(indentation + 4)).append('\n');

        if (inheritance != null) {
            builder.append(indent).append("Inherit:\n")
                    .append(inheritance.describe(indentation + 4));
            if (statements != null) {
                builder.append('\n');
            }
        }
        if (statements != null) {
            builder.append(indent).append("Statements:\n");
            for (int i = 0; i < statements.length; ++i) {
                builder.append(statements[i].describe(indentation + 4));
                if (i + 1 < statements.length) {
                    builder.append('\n');
                }
            }
        }
        return builder.toString();
    }
}
