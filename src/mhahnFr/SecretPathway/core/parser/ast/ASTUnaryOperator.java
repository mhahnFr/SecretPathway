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

import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.utils.StreamPosition;

/**
 * This class represents a unary operator as AST node.
 *
 * @author mhahnFr
 * @since 07.02.23
 */
public class ASTUnaryOperator extends ASTExpression {
    /** The type of the operator.                        */
    private final TokenType operatorType;
    /** The identifier to which the operator is applied. */
    private final ASTExpression identifier;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin        the beginning position
     * @param operatorType the type of this operator
     * @param identifier   the identifier to which to apply this operator
     */
    public ASTUnaryOperator(final StreamPosition begin,
                            final TokenType      operatorType,
                            final ASTExpression  identifier) {
        super(begin, identifier.getEnd(), ASTType.UNARY_OPERATOR);

        this.operatorType = operatorType;
        this.identifier   = identifier;
    }

    /**
     * Returns the type of this operator.
     *
     * @return the type of this operator
     */
    public TokenType getOperatorType() {
        return operatorType;
    }

    /**
     * Returns the identifier to which this operator is applied to.
     *
     * @return the identifier to apply this operator to
     */
    public ASTExpression getIdentifier() {
        return identifier;
    }

    @Override
    public void visit(ASTVisitor visitor) {
        if (visitor.maybeVisit(this)) {
            identifier.visit(visitor);
        }
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " " + operatorType + "\n" +
               identifier.describe(indentation + 4);
    }
}
