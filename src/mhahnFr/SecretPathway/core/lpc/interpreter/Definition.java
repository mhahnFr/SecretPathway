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
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDefinition;

/**
 * This class defines a declaration.
 *
 * @author mhahnFr
 * @since 28.02.23
 */
public class Definition extends Instruction {
    /** The name of the declared identifier. */
    private final String name;
    /** The type of the declared identifier. */
    private final ASTType type;

    /**
     * Constructs this definition using the given information.
     *
     * @param begin      the beginning position
     * @param name       the name
     * @param returnType the type the identifier evaluates to
     * @param type       the AST type of the definition
     */
    public Definition(final int               begin,
                      final String            name,
                      final ASTTypeDefinition returnType,
                      final ASTType           type) {
        super(begin);
        setReturnType(returnType);

        this.name       = name;
        this.type       = type;
    }

    /**
     * Returns the name of this definition.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the AST type of this definition.
     *
     * @return the AST type
     */
    public ASTType getType() {
        return type;
    }
}
