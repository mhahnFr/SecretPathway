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

package mhahnFr.SecretPathway.core.lpc.interpreter;

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTExpression;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTType;

/**
 * This class represents an AST node as highlighted element.
 *
 * @author mhahnFr
 * @since 04.03.23
 */
public class ASTHighlight extends Highlight<ASTType> {
    /**
     * Constructs this highlight element using the given bounds
     * and the given type.
     *
     * @param begin the beginning position
     * @param end   the end position
     * @param type  the type
     */
    public ASTHighlight(int begin, int end, ASTType type) {
        super(begin, end, type);
    }

    /**
     * Constructs this highlight element from the given
     * {@link ASTExpression}.
     *
     * @param expression the expression to be highlighted
     */
    public ASTHighlight(final ASTExpression expression) {
        super(expression.getBegin().position(), expression.getEnd().position(), expression.getASTType());
    }
}
