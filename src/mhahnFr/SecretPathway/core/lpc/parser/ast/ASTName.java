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
import mhahnFr.utils.StreamPosition;

/**
 * This class represents a name as an AST node.
 *
 * @author mhahnFr
 * @since 02.02.23
 */
public class ASTName extends ASTExpression {
    /** The represented name. */
    private final String name;

    /**
     * Constructs this node using the name token to be
     * represented. Throws no exception if no name is given.
     *
     * @param name the name token to be represented
     */
    public ASTName(final Token name) {
        super(name.beginPos(), name.endPos(), ASTType.NAME);

        this.name = (String) name.payload();
    }

    public ASTName(final StreamPosition begin,
                   final StreamPosition end) {
        super(begin, end, ASTType.NAME);

        this.name = null;
    }

    /**
     * Returns the represented name.
     *
     * @return the represented name
     */
    public String getName() {
        return name;
    }

    @Override
    public String describe(int indentation) {
        return super.describe(indentation) + " " + name;
    }
}
