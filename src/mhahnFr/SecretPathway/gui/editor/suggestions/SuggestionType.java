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

package mhahnFr.SecretPathway.gui.editor.suggestions;

import java.util.Arrays;

/**
 * This enumeration contains all possible suggestion types.
 *
 * @author mhahnFr
 * @since 04.04.23
 */
public enum SuggestionType {
    /** Represents a literal value, no suggestions.    */
    LITERAL,
    /** Represents anything, all kinds of suggestions. */
    ANY,
    /** Represents identifiers and literal values.     */
    LITERAL_IDENTIFIER,
    /** Represents identifiers.                        */
    IDENTIFIER,
    /** Represents types.                              */
    TYPE,
    /** Represents modifiers.                          */
    MODIFIER,
    /** Represents modifiers and types.                */
    TYPE_MODIFIER;

    /**
     * Returns whether this suggestion type is one of the given types.
     *
     * @param other the other types
     * @return whether this type is one of the given ones
     */
    public boolean is(final SuggestionType... other) {
        return Arrays.asList(other).contains(this);
    }
}
