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

package mhahnFr.SecretPathway.core.lpc.interpreter;

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDeclaration;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.utils.StreamPosition;

/**
 * This class represents a literal return type.
 *
 * @author mhahnFr
 * @since 18.03.23
 */
public class ReturnType extends ASTTypeDeclaration {
    /**
     * Constructs this return type using the given type literal.
     *
     * @param type the type
     */
    public ReturnType(final TokenType type) {
        super(new Token(new StreamPosition(0, ""),
                        type,
                       null,
                        new StreamPosition(0, "")),
              false);
    }
}
