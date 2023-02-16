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

import mhahnFr.SecretPathway.core.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.utils.StreamPosition;

import java.util.List;

public class ASTFunctionReferenceType extends ASTExpression {
    private final TokenType returnType;
    private final List<ASTExpression> callTypes;

    public ASTFunctionReferenceType(final Token               returnType,
                                    final List<ASTExpression> callTypes,
                                    final StreamPosition      end) {
        super(returnType.beginPos(), end, ASTType.FUNCTION_REFERENCE);

        this.returnType = returnType.type();
        this.callTypes  = callTypes;
    }

    public TokenType getReturnType() {
        return returnType;
    }

    public List<ASTExpression> getCallTypes() {
        return callTypes;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            if (callTypes != null) {
                final var iterator = callTypes.listIterator();
                while (iterator.hasNext()) {
                    iterator.next().visit(visitor);
                }
            }
        }
    }

    @Override
    public String describe(int indentation) {
        final var builder = new StringBuilder();

        builder.append(super.describe(indentation)).append(" Return type: ").append(returnType).append('\n');

        if (callTypes != null) {
            builder.append(" ".repeat(Math.max(0, indentation))).append("Parameter types:\n");

            final var iterator = callTypes.listIterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next().describe(indentation + 4));
                if (iterator.hasNext()) {
                    builder.append('\n');
                }
            }
        }
        return builder.toString();
    }
}
