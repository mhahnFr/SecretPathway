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

import mhahnFr.utils.StreamPosition;

/**
 * This class represents an array expression as an AST node.
 *
 * @author mhahnFr
 * @since 09.02.23
 */
public class ASTArray extends ASTExpression {
    /** The content expressions. */
    private final ASTExpression[] content;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin   the beginning position
     * @param end     the end position
     * @param content the content expressions
     */
    public ASTArray(final StreamPosition  begin,
                    final StreamPosition  end,
                    final ASTExpression[] content) {
        super(begin, end, ASTType.ARRAY);

        this.content = content;
    }

    /**
     * Returns the content expressions of this array.
     *
     * @return the content expressions
     */
    public ASTExpression[] getContent() {
        return content;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            if (content != null) {
                for (int i = 0; i < content.length; ++i) {
                    content[i].visit(visitor);
                }
            }
        }
    }
}
