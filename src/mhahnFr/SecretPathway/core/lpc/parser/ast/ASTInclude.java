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
 * This class represents an include statement.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class ASTInclude extends ASTExpression {
    /** The raw value of the inclusion. */
    private final ASTExpression included;

    /**
     * Constructs this AST node using the given positions and
     * the given inclusion string.
     *
     * @param begin    the beginning position
     * @param end      the end position
     * @param included the inclusion string
     */
    public ASTInclude(final StreamPosition begin,
                      final StreamPosition end,
                      final ASTExpression  included) {
        super(begin, end, ASTType.AST_INCLUDE);

        this.included = included;
    }

    /**
     * Returns the raw inclusion string.
     *
     * @return the inclusion string
     */
    public ASTExpression getIncluded() {
        return included;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            if (included != null) {
                included.visit(visitor);
            }
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " included:\n" +
                (included == null ? " ".repeat(Math.max(0, indentation + 4)) + "nothing" : included.describe(indentation + 4));
    }

    @Override
    public boolean hasSubExpressions() {
        return true;
    }

    @Override
    public List<ASTExpression> getSubExpressions() {
        return Collections.singletonList(included);
    }
}
