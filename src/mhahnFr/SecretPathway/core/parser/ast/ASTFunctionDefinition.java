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
 * This class represents an AST node for a function
 * definition.
 *
 * @author mhahnFr
 * @since 28.01.23
 */
public class ASTFunctionDefinition extends ASTExpression {
    /** The name of this declared function.           */
    private final String name;
    /** The return type of this function.             */
    private final TokenType type;
    /** The expressions in the body of this function. */
    private final ASTExpression[] body;
    /** The declared parameters of this function.     */
    private final ASTExpression[] parameters;

    /**
     * Constructs this AST node using the given positions,
     * the given return type, name and the given body expressions.
     *
     * @param begin the beginning position of this expression
     * @param end the end position of this expression
     * @param type the return type of the declared function
     * @param name the name of the declared function
     * @param parameters the declared parameters
     * @param body the instructions of the body of this function
     */
    public ASTFunctionDefinition(final StreamPosition  begin,
                                 final StreamPosition  end,
                                 final TokenType       type,
                                 final String          name,
                                 final ASTExpression[]  parameters,
                                 final ASTExpression[] body) {
        super(begin, end);

        this.type       = type;
        this.name       = name;
        this.body       = body;
        this.parameters = parameters;
    }

    /**
     * Returns the name of this declared function.
     *
     * @return the name of this function
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the return type of this declared function.
     *
     * @return the return type of this function
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the {@link ASTExpression}s in the body of
     * this function.
     *
     * @return the expressions in this function's body
     */
    public ASTExpression[] getBody() {
        return body;
    }

    /**
     * Returns the declare parameters of this declared function.
     *
     * @return the declared parameters
     */
    public ASTExpression[] getParameters() {
        return parameters;
    }
}
