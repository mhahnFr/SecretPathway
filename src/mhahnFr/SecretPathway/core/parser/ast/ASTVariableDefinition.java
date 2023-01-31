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
 * This class represents an AST node for a variable definition.
 *
 * @author mhahnFr
 * @since 28.01.23
 */
public class ASTVariableDefinition extends ASTExpression {
    /** The name of this declared variable. */
    private final String name;
    /** The declared type of this variable. */
    private final TokenType type;

    /**
     * Constructs this AST node using the given positions,
     * the given name and type.
     *
     * @param begin the beginning position of this expression
     * @param end the end position of this expression
     * @param type the declared type of this variable
     * @param name the declared name of this variable
     */
    public ASTVariableDefinition(final StreamPosition begin,
                                 final StreamPosition end,
                                 final TokenType      type,
                                 final String         name) {
        super(begin, end, ASTType.VARIABLE_DEFINITION);

        this.type = type;
        this.name = name;
    }

    /**
     * Returns the name of this declared variable.
     *
     * @return the name of this variable
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this declared variable.
     *
     * @return the type of this variable
     */
    public TokenType getType() {
        return type;
    }
}
