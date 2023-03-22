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

import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;

/**
 * This class represents a parenthesized suggestion.
 *
 * @author mhahnFr
 * @since 13.03.23
 */
public class ParenthesizedSuggestion implements Suggestion {
    /** The {@link TokenType} to be suggested. */
    private final TokenType keyword;
    /** The suggested string.                  */
    private final String string;

    /**
     * Initializes this suggestion using the given {@link TokenType}.
     *
     * @param keyword the suggested keyword
     */
    public ParenthesizedSuggestion(final TokenType keyword) {
        this.keyword = keyword;
        this.string  = keyword.toString().toLowerCase();
    }

    @Override
    public String getSuggestion() {
        return string + " ()";
    }

    @Override
    public String getDescription() {
        return string + " statement";
    }

    @Override
    public int getRelativeCursorPosition() {
        return string.length() + 2;
    }

    /**
     * Returns the suggested token.
     *
     * @return the suggested token
     */
    public TokenType getToken() {
        return keyword;
    }
}
