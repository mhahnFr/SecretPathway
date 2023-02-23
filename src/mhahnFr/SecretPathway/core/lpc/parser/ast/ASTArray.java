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
 * This class represents an array expression as an AST node.
 *
 * @author mhahnFr
 * @since 09.02.23
 */
public class ASTArray extends ASTExpression {
    /** The content expressions. */
    private final List<ASTExpression> content;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin   the beginning position
     * @param end     the end position
     * @param content the content expressions
     */
    public ASTArray(final StreamPosition      begin,
                    final StreamPosition      end,
                    final List<ASTExpression> content) {
        super(begin, end, ASTType.ARRAY);

        this.content = content;
    }

    /**
     * Returns the content expressions of this array.
     *
     * @return the content expressions
     */
    public List<ASTExpression> getContent() {
        return content;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            if (content != null) {
                final var iterator = content.listIterator();
                while (iterator.hasNext()) {
                    iterator.next().visit(visitor);
                }
            }
        }
    }

    @Override
    public String describe(final int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" [\n");
        if (content != null) {
            final var iterator = content.listIterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next().describe(indentation + 4)).append('\n');
            }
        }
        builder.append(" ".repeat(Math.max(0, indentation))).append(']');

        return builder.toString();
    }
}
