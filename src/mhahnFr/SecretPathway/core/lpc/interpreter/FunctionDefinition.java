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

import java.awt.*;
import java.util.List;

/**
 * This class represents a function definition.
 *
 * @author mhahnFr
 * @since 08.03.23
 */
public class FunctionDefinition extends Definition {
    /** The list with the parameter definitions. */
    private final List<Definition> parameters;

    /**
     * Constructs this function definition using the given information.
     *
     * @param begin      the beginning position
     * @param name       the name of the function
     * @param returnType the return type of the function
     * @param parameters the list with the parameter definitions
     */
    public FunctionDefinition(final int               begin,
                              final String            name,
                              final ASTTypeDefinition returnType,
                              final List<Definition>  parameters) {
        super(begin, name, returnType, ASTType.FUNCTION_DEFINITION);

        this.parameters = parameters;
    }

    /**
     * Returns the parameter definitions of this function definition.
     *
     * @return the parameter definitions
     */
    public List<Definition> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder(super.getName());

        builder.append('(');

        final var iterator = parameters.listIterator();
        while (iterator.hasNext()) {
            final var parameter = iterator.next();

            final String typeString;
            final var type = parameter.getReturnType();
            final String ts;
            if (type == null || (ts = type.toString()) == null) {
                typeString = "<< unknown >>";
            } else {
                typeString = ts;
            }
            builder.append(typeString).append(' ').append(parameter.getName());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(')');

        return builder.toString();
    }
}
