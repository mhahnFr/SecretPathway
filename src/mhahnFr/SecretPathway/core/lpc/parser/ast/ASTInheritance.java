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

import java.util.Collections;
import java.util.List;

/**
 * This class represents an inheritance node.
 *
 * @author mhahnFr
 * @since 27.01.23
 */
public class ASTInheritance extends ASTExpression {
    /** The inheritance string. */
    private final ASTExpression inherited;

    /**
     * Constructs this AST node using the given positions
     * and the inheritance string.
     *
     * @param begin     the beginning position
     * @param end       the end position
     * @param inherited the inheritance string
     */
    public ASTInheritance(final StreamPosition begin,
                          final StreamPosition end,
                          final ASTExpression  inherited) {
        super(begin, end, ASTType.AST_INHERITANCE);

        this.inherited = inherited;
    }

    /**
     * Returns the inheritance string.
     *
     * @return the inheritance string
     */
    public ASTExpression getInherited() {
        return inherited;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            if (inherited != null) {
                inherited.visit(visitor);
            }
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " inheriting from:\n" +
                (inherited == null ? " ".repeat(Math.max(0, indentation + 4)) + "nothing" : inherited.describe(indentation + 4));
    }

    @Override
    public boolean hasSubExpressions() {
        return true;
    }

    @Override
    public List<ASTExpression> getSubExpressions() {
        return Collections.singletonList(inherited);
    }
}
