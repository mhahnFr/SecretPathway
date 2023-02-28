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

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTType;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;

public class Definition extends Instruction {
    private final String name;
    private final ASTType type;

    public Definition(final int       begin,
                      final String    name,
                      final TokenType returnType,
                      final ASTType   type) {
        super(begin);
        setReturnType(returnType);

        this.name       = name;
        this.type       = type;
    }

    public String getName() {
        return name;
    }

    public ASTType getType() {
        return type;
    }
}
