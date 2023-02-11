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

/**
 * This class represents a modifier as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTModifier extends ASTExpression {
    /** The represented modifier. */
    private final TokenType modifier;

    /**
     * Constructs this AST node using the given modifier.
     * If no modifier is given, no exception is thrown.
     *
     * @param modifier the modifier token to be represented
     */
    public ASTModifier(final Token modifier) {
        super(modifier == null ? null : modifier.beginPos(),
              modifier == null ? null : modifier.endPos(),
              ASTType.MODIFIER);

        this.modifier = modifier == null ? null : modifier.type();
    }

    /**
     * Returns the represented modifier.
     *
     * @return the represented modifier
     */
    public TokenType getModifier() {
        return modifier;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " " + modifier;
    }
}
