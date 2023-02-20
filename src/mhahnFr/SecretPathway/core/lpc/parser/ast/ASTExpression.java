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

/**
 * This class represents a node in the AST.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public abstract class ASTExpression {
    /** The beginning position of this expression. */
    private final StreamPosition begin;
    /** The end position of this expression.       */
    private final StreamPosition end;
    /** The type of this AST node.                 */
    private final ASTType type;

    /**
     * Constructs this expression using the given positions.
     *
     * @param begin the beginning position
     * @param end the end position
     * @param type the type of this AST node
     */
    protected ASTExpression(StreamPosition begin, StreamPosition end, ASTType type) {
        this.begin = begin;
        this.end   = end;
        this.type  = type;
    }

    /**
     * Returns the end position of this expression.
     *
     * @return the end position
     */
    public StreamPosition getEnd() {
        return end;
    }

    /**
     * Returns the beginning position of this expression.
     *
     * @return the beginning position
     */
    public StreamPosition getBegin() {
        return begin;
    }

    /**
     * Returns the type of this AST node.
     *
     * @return the type of this node
     */
    public ASTType getASTType() {
        return type;
    }

    /**
     * Visits this AST node using the given {@link ASTVisitor}.
     * Subclasses might want to override this method to make
     * the given visitor visit contained subexpressions.
     *
     * @param visitor the visitor
     */
    public void visit(final ASTVisitor visitor) {
        visitor.visit(this);
    }

    public String describe(final int indentation) {
        return " ".repeat(Math.max(0, indentation)) + type + " [" + begin.position() + " - " + end.position() + "]";
    }
}
