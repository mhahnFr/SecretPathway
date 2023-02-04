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

import java.util.List;

public class ASTBlock extends ASTExpression {
    private final List<ASTExpression> body;

    public ASTBlock(final StreamPosition begin,
                    final StreamPosition end,
                    final List<ASTExpression> body) {
        super(begin, end, ASTType.BLOCK);

        this.body = body;
    }

    public List<ASTExpression> getBody() {
        return body;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            final var iterator = body.listIterator();
            while (iterator.hasNext()) {
                iterator.next().visit(visitor);
            }
        }
    }
}
