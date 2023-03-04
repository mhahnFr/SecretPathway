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

/**
 * This class represents an interpretation statement as
 * highlighting element.
 *
 * @author mhahnFr
 * @since 04.03.23
 */
public class InterpretationHighlight extends Highlight<InterpretationType> {
    /**
     * Constructs this highlighting element using the given
     * bounds and the given type.
     *
     * @param begin the beginning position
     * @param end   the end position
     * @param type  the type
     */
    public InterpretationHighlight(int begin, int end, InterpretationType type) {
        super(begin, end, type);
    }
}
