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
 * This class represents a type as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTTypeDeclaration extends ASTExpression {
    /** The represented type. */
    private final TokenType type;

    /**
     * Constructs this AST node. If no type is given, no
     * exception is thrown.
     *
     * @param type the represented type
     */
    public ASTTypeDeclaration(final Token type) {
        super(type.beginPos(), type.endPos(), ASTType.TYPE);

        this.type = type.type();
    }

    public ASTTypeDeclaration(final StreamPosition begin,
                              final StreamPosition end) {
        super(begin, end, ASTType.TYPE);

        this.type = null;
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
        return super.describe(indentation) + " " + type;
    }
}
