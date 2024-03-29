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

import java.util.Objects;

/**
 * This interface defines a suggestion.
 *
 * @author mhahnFr
 * @since 03.03.23
 */
public interface Suggestion {
    /**
     * Returns the suggested text.
     *
     * @return the suggested text
     */
    String getSuggestion();

    /**
     * Returns the description of the suggestion to be displayed.
     *
     * @return the display string of this suggestion
     */
    default String getDescription() {
        return getSuggestion();
    }

    /**
     * Returns the relative cursor position for this suggestion.
     * {@code -1} indicates no cursor movement.
     *
     * @return the desired relative cursor position
     */
    default int getRelativeCursorPosition() {
        return -1;
    }

    /**
     * Returns a text to be displayed on the right site
     * of the {@link SuggestionsWindow}.
     *
     * @return the right site {@link String}
     */
    default String getRightSite() {
        return "";
    }

    /**
     * Checks the given other {@link Suggestion} for equality
     * with this suggestion.
     *
     * @param other the other suggestion to be compared
     * @return whether the given suggestion is equal to this one
     */
    default boolean equals(final Suggestion other) {
        return other != null &&
               Objects.equals(getSuggestion(),  other.getSuggestion())  &&
               Objects.equals(getDescription(), other.getDescription()) &&
               Objects.equals(getRightSite(),   other.getRightSite())   &&
               getRelativeCursorPosition() == other.getRelativeCursorPosition();
    }
}
