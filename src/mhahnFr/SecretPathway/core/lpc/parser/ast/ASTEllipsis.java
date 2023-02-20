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
 * This class represents an ellipsis AST node.
 *
 * @author mhahnFr
 * @since 01.02.23
 */
public class ASTEllipsis extends ASTExpression {
    /**
     * Constructs this AST node.
     *
     * @param ellipsis the {@link Token} representing the actual ellipsis
     */
    public ASTEllipsis(final Token ellipsis) {
        super(ellipsis.beginPos(), ellipsis.endPos(), ASTType.AST_ELLIPSIS);
    }
}
