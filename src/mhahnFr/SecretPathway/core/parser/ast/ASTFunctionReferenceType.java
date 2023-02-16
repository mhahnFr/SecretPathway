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

/**
 * This class represents a function reference type as an AST node.
 *
 * @author mhahnFr
 * @since 16.02.23
 */
public class ASTFunctionReferenceType extends ASTExpression {
    /** The return type of the referred function. */
    private final TokenType returnType;
    /** The potential argument types.             */
    private final List<ASTExpression> callTypes;
    private final boolean returnArray;

    /**
     * Constructs this AST node using the given information.
     *
     * @param returnType the return type of the referred function
     * @param callTypes  the potential argument types
     * @param end        the end position
     */
    public ASTFunctionReferenceType(final Token               returnType,
                                    final boolean             array,
                                    final List<ASTExpression> callTypes,
                                    final StreamPosition      end) {
        super(returnType.beginPos(), end, ASTType.FUNCTION_REFERENCE);

        this.returnType  = returnType.type();
        this.callTypes   = callTypes;
        this.returnArray = array;
    }

    /**
     * Returns the return type of the referred function.
     *
     * @return the return type
     */
    public TokenType getReturnType() {
        return returnType;
    }

    /**
     * Returns the list with the potential argument types.
     *
     * @return the argument types
     */
    public List<ASTExpression> getCallTypes() {
        return callTypes;
    }

    public boolean isReturnArray() {
        return returnArray;
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

        builder.append(super.describe(indentation)).append(" Return type: ").append(returnType);
        if (returnArray) {
            builder.append("[]");
        }
        builder.append('\n');

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
