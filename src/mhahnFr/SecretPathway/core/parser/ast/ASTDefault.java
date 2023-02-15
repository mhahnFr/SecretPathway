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
 * This class represents a {@code default} case expression
 * as an AST node.
 *
 * @author mhahnFr
 * @since 15.02.23
 */
public class ASTDefault extends ASTExpression {
    /**
     * Constructs this AST node using the given {@link Token}.
     *
     * @param token the token to be represented by this node
     */
    public ASTDefault(final Token token) {
        super(token.beginPos(), token.beginPos(), ASTType.DEFAULT);
    }
}
