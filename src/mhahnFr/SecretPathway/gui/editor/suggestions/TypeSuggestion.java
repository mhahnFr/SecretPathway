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
import mhahnFr.SecretPathway.gui.editor.suggestions.Suggestion;

/**
 * This record represents a {@link TokenType} suggestion.
 *
 * @param type the type to suggest
 */
public record TypeSuggestion(TokenType type) implements Suggestion {
    @Override
    public String getSuggestion() {
        return switch(type) {
            case INT_KEYWORD    -> "int";
            case CHAR_KEYWORD   -> "char";
            case SYMBOL_KEYWORD -> "symbol";

            default -> type.toString().toLowerCase();
        } + " ";
    }
}
