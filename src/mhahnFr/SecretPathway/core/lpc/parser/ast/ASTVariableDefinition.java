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
 * This class represents a variable definition as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTVariableDefinition extends ASTExpression {
    /** The modifiers of this variable. */
    private final List<ASTExpression> modifiers;
    /** The type of this variable.      */
    private final ASTExpression type;
    /** The name of this variable.      */
    private final ASTExpression name;

    /**
     * Constructs this AST node for variable definitions using the
     * given information.
     *
     * @param begin     the beginning position of this node
     * @param end       the end position of this node
     * @param modifiers the declared modifiers of this variable definition
     * @param type      the declared type of this variable definition
     * @param name      the declared name of this variable definition
     */
    public ASTVariableDefinition(final StreamPosition      begin,
                                 final StreamPosition      end,
                                 final List<ASTExpression> modifiers,
                                 final ASTExpression       type,
                                 final ASTExpression       name) {
        super(begin, end, ASTType.VARIABLE_DEFINITION);

        this.modifiers = modifiers;
        this.type      = type;
        this.name      = name;
    }

    /**
     * Returns the declared modifiers of this declared variable.
     *
     * @return the declared modifiers
     */
    public List<ASTExpression> getModifiers() {
        return modifiers;
    }

    /**
     * Returns the declared type of this declared variable.
     *
     * @return the declared type
     */
    public ASTExpression getType() {
        return type;
    }

    /**
     * Returns the declared name of this declared variable.
     *
     * @return the declared name
     */
    public ASTExpression getName() {
        return name;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {

            if (modifiers != null) {
                modifiers.forEach(e -> e.visit(visitor));
            }

            if (type != null) {
                type.visit(visitor);
            }

            name.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation));
        if (modifiers != null) {
            builder.append(" modifiers:\n");
            modifiers.forEach(e -> builder.append(e.describe(indentation + 4)).append('\n'));
        }
        if (type != null) {
            builder.append((modifiers == null ? "" : " ".repeat(Math.max(0, indentation)))).append(" type:\n")
                   .append(type.describe(indentation + 4)).append('\n');
        }
        builder.append((type == null && modifiers == null ? "" : " ".repeat(Math.max(0, indentation)))).append(" name:\n")
               .append(name.describe(indentation + 4));

        return builder.toString();
    }
}
