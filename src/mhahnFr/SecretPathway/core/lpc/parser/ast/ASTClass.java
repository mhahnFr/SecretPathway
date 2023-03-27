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
    private final List<ASTExpression> statements;

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
        super(begin, (inheritance == null ? name : inheritance).getEnd(), ASTType.AST_CLASS);

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
                    final List<ASTExpression> statements) {
        super(begin, (statements.isEmpty() ? name : statements.get(statements.size() - 1)).getEnd(), ASTType.AST_CLASS);

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
    public List<ASTExpression> getStatements() {
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
                statements.forEach(e -> e.visit(visitor));
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
            final var iterator = statements.listIterator();
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
