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

/**
 * This class represents a combination of {@link ASTExpression}s.
 * Mostly used to fill holes in the AST (in other words, with syntax errors).
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class ASTCombination extends ASTExpression {
    /** The {@link ASTExpression}s this combination is made of. */
    private final ASTExpression[] expressions;

    /**
     * Constructs this expression using the given sub-expressions.
     *
     * @param expressions the sub-expressions
     */
    public ASTCombination(ASTExpression... expressions) {
        super(expressions[0].getBegin(), expressions[expressions.length - 1].getEnd(), ASTType.COMBINATION);

        this.expressions = expressions;
    }

    /**
     * Returns the sub-expressions this combination is made of.
     *
     * @return the sub expressions
     */
    public ASTExpression[] getExpressions() {
        return expressions;
    }
}
