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

public class ASTVariableDefinitionV2 extends ASTExpression {
    private final List<ASTExpression> modifiers;
    private final ASTExpression type;
    private final ASTExpression name;

    public ASTVariableDefinitionV2(final StreamPosition      begin,
                                   final StreamPosition      end,
                                   final List<ASTExpression> modifiers,
                                   final ASTExpression       type,
                                   final ASTExpression       name) {
        super(begin, end, ASTType.VARIABLE_DEFINITION);

        this.modifiers = modifiers;
        this.type      = type;
        this.name      = name;
    }

    public List<ASTExpression> getModifiers() {
        return modifiers;
    }

    public ASTExpression getType() {
        return type;
    }

    public ASTExpression getName() {
        return name;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            final var iterator = modifiers.listIterator();
            while (iterator.hasNext()) {
                iterator.next().visit(visitor);
            }

            type.visit(visitor);
            name.visit(visitor);
        }
    }
}
