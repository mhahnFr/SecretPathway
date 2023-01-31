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
 * This class represents a parameter AST node.
 *
 * @author mhahnFr
 * @since 30.01.23
 */
public class ASTParameter extends ASTExpression {
    /** The type of this parameter. */
    private final TokenType type;
    /** The name of this parameter. */
    private final String name;

    /**
     * Constructs this AST node using the given positions,
     * the type and the name.
     *
     * @param begin the beginning position
     * @param end the end position
     * @param type the declared type
     * @param name the declared name
     */
    public ASTParameter(final StreamPosition begin,
                        final StreamPosition end,
                        final TokenType      type,
                        final String         name) {
        super(begin, end, ASTType.PARAMETER);

        this.type = type;
        this.name = name;
    }

    /**
     * Returns the declared name fo this parameter.
     *
     * @return the declared name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the declared type of this parameter.
     *
     * @return the declared type
     */
    public TokenType getType() {
        return type;
    }
}
