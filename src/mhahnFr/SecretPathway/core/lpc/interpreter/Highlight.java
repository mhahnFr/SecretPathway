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
 * This class defines a highlighting information.
 *
 * @param <T> the used highlighting type
 * @author mhahnFr
 * @since 04.03.23
 */
public class Highlight<T extends HighlightType> {
    /** The beginning position of this highlight element.           */
    private final int begin;
    /** The end position of this highlight element.                 */
    private final int end;
    /** The type as which this highlight element should be treated. */
    private final T type;

    /**
     * Constructs this highlighting element using the given position
     * and the type.
     *
     * @param begin the beginning position
     * @param end   the end position
     * @param type  the type
     */
    public Highlight(final int begin, final int end, final T type) {
        this.begin = begin;
        this.end   = end;
        this.type  = type;
    }

    /**
     * Returns the beginning position of this highlighting element.
     *
     * @return the beginning position
     */
    public int getBegin() {
        return begin;
    }

    /**
     * Returns the end position of this highlighting element.
     *
     * @return the end position
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns the type as which this element should be highlighted.
     *
     * @return the type
     */
    public T getType() {
        return type;
    }
}
