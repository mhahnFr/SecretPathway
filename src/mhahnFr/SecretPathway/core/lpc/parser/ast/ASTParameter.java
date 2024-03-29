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

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a declared function parameter
 * as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTParameter extends ASTExpression {
    /** The declared type of this parameter. */
    private final ASTExpression type;
    /** The declared name of this parameter. */
    private final ASTExpression name;

    /**
     * Constructs this AST node using the given information.
     *
     * @param type the declared type
     * @param name the declared name
     */
    public ASTParameter(final ASTExpression type,
                        final ASTExpression name) {
        super(type.getBegin(), name.getEnd(), ASTType.PARAMETER);

        this.type = type;
        this.name = name;
    }

    /**
     * Returns the declared type of this declared parameter.
     *
     * @return the declared type
     */
    public ASTExpression getType() {
        return type;
    }

    /**
     * Returns the declared name of this parameter.
     *
     * @return the declared parameter
     */
    public ASTExpression getName() {
        return name;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            type.visit(visitor);
            name.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " type:\n" +
               type.describe(indentation + 4) + "\n" +
               " ".repeat(Math.max(0, indentation)) + "name:\n" +
               name.describe(indentation + 4);
    }

    @Override
    public boolean hasSubExpressions() {
        return true;
    }

    @Override
    public List<ASTExpression> getSubExpressions() {
        return Arrays.asList(type, name);
    }
}
