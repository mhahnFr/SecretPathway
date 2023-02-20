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

import mhahnFr.utils.StreamPosition;

/**
 * This class represents an inheritance node.
 *
 * @author mhahnFr
 * @since 27.01.23
 */
public class ASTInheritance extends ASTExpression {
    /** The inheritance string. */
    private final String inherited;

    /**
     * Constructs this AST node using the given positions
     * and the inheritance string.
     *
     * @param begin the beginning position
     * @param end the end position
     * @param inherited the inheritance string
     */
    public ASTInheritance(StreamPosition begin, StreamPosition end, final String inherited) {
        super(begin, end, ASTType.AST_INHERITANCE);

        this.inherited = inherited;
    }

    /**
     * Returns the inheritance string.
     *
     * @return the inheritance string
     */
    public String getInherited() {
        return inherited;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " inheriting from \"" + inherited + "\"";
    }
}
