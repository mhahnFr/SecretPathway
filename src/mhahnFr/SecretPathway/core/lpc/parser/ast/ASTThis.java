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

/**
 * This class represents a "this" expression as an AST node.
 *
 * @author mhahnFr
 * @since 08.02.23
 */
public class ASTThis extends ASTExpression {
    /**
     * Constructs this AST node using the given token.
     *
     * @param token the token to be represented by this AST node
     */
    public ASTThis(final Token token) {
        super(token.beginPos(), token.endPos(), ASTType.AST_THIS);
    }
}
