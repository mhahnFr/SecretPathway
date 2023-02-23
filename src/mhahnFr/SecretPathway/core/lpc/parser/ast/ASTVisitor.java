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

/**
 * This interface defines an AST visitor.
 *
 * @author mhahnFr
 * @since 31.01.23
 */
public interface ASTVisitor {
    /**
     * Visits the given {@link ASTExpression}.
     *
     * @param expression the expression to be visited
     */
    void visit(final ASTExpression expression);

    /**
     * Returns whether the given type of AST node should be
     * visited. Default implementation returns {@code true}.
     *
     * @param type the type in question
     * @return whether to visit the given type in depth
     */
    default boolean visitType(final ASTType type) {
        return true;
    }

    /**
     * Visits the given {@link ASTExpression} and returns whether
     * to visit contained {@link ASTExpression}s.
     *
     * @param expression the node to be visited
     * @return whether to visit contained nodes
     */
    default boolean maybeVisit(final ASTExpression expression) {
        visit(expression);
        return visitType(expression.getASTType());
    }
}
