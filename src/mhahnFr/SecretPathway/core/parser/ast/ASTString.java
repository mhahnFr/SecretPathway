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

/**
 * This class represents a string expression as an AST node.
 *
 * @author mhahnFr
 * @since 08.02.23
 */
public class ASTString extends ASTExpression {
    /** The represented value. */
    private final String value;

    /**
     * Constructs this AST node using the given token.
     *
     * @param token the token to be represented as AST node
     */
    public ASTString(final Token token) {
        super(token.beginPos(), token.endPos(), ASTType.AST_STRING);

        this.value = (String) token.payload();
    }

    /**
     * Returns the represented value.
     *
     * @return the represented value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " \"" + value + "\"";
    }
}
