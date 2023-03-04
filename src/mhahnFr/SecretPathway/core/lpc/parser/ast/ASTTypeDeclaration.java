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

package mhahnFr.SecretPathway.core.lpc.parser.ast;

import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.utils.StreamPosition;

/**
 * This class represents a type as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTTypeDeclaration extends ASTExpression {
    /** The represented type.                               */
    private final TokenType type;
    /** Indicates whether the represented type is an array. */
    private final boolean isArray;

    /**
     * Constructs this AST node. If no type is given, no
     * exception is thrown.
     *
     * @param type    the represented type
     * @param isArray whether the type is an array type
     */
    public ASTTypeDeclaration(final Token   type,
                              final boolean isArray) {
        super(type.beginPos(), type.endPos(), ASTType.TYPE);

        this.type    = type.type();
        this.isArray = isArray;
    }

    /**
     * Constructs this AST node using the given positions.
     *
     * @param begin the beginning position
     * @param end   the end position
     */
    public ASTTypeDeclaration(final StreamPosition begin,
                              final StreamPosition end) {
        super(begin, end, ASTType.TYPE);

        this.type    = null;
        this.isArray = false;
    }

    /**
     * Returns the represented type.
     *
     * @return the represented type
     */
    public TokenType getType() {
        return type;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " " + type + (isArray ? "[]" : "");
    }

    /**
     * Returns whether the represented type is an array.
     *
     * @return whether an array type is represented
     */
    public boolean isArray() {
        return isArray;
    }
}
