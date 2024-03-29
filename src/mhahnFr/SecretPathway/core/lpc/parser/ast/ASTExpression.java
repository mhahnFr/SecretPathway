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

    /**
     * Describes this AST node using the given indentation.
     * Subclasses might want to override this method to describe
     * contained subexpression.
     *
     * @param indentation the level of the indentation
     * @return a {@link String} describing this AST node human-readable
     */
    public String describe(final int indentation) {
        return " ".repeat(Math.max(0, indentation)) + type + " [" + begin.position() + " - " + end.position() + "]";
    }

    /**
     * Returns whether this AST node contains other expressions.
     * They can be queried using {@link #getSubExpressions()}.
     *
     * @return whether this node contains sub-expressions
     * @see #getSubExpressions()
     */
    public boolean hasSubExpressions() {
        return false;
    }

    /**
     * Returns all sub-expressions of this node. The order
     * of the sub-expressions is not defined. If this node
     * does not contain sub-expressions, {@code null} is returned.
     * Check for sub-expressions using {@link #hasSubExpressions()}.
     *
     * @return a list with all sub-expressions
     * @see #hasSubExpressions()
     */
    public List<ASTExpression> getSubExpressions() {
        return null;
    }
}
