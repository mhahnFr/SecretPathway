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

/**
 * This class represents an operator identifier as an AST node.
 *
 * @author mhahnFr
 * @since 17.02.23
 */
public class ASTOperatorName extends ASTExpression {
    /** The type of this operator. */
    private final TokenType type;

    /**
     * Constructs this AST node using the given information.
     *
     * @param begin    the beginning position
     * @param operator the represented operator
     */
    public ASTOperatorName(final StreamPosition begin,
                           final Token          operator) {
        super(begin, operator.endPos(), ASTType.OPERATOR_NAME);

        this.type = operator.type();
    }

    /**
     * Returns the type of the represented operator.
     *
     * @return the type
     */
    public TokenType getType() {
        return type;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " Type: " + type;
    }
}
