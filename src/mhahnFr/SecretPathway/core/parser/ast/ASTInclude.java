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
 * This class represents an include statement.
 *
 * @author mhahnFr
 * @since 26.01.23
 */
public class ASTInclude extends ASTExpression {
    private final String included;

    public ASTInclude(final StreamPosition begin, final StreamPosition end, final String included) {
        super(begin, end);

        this.included = included;
    }

    public String getIncluded() {
        return included;
    }
}
