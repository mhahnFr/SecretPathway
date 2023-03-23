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

public class ValueReturnSuggestion extends ReturnSuggestion {
    private final Object value;

    public ValueReturnSuggestion() {
        this(null);
    }

    public ValueReturnSuggestion(final Object value) {
        this.value = value;
    }

    @Override
    public String getSuggestion() {
        return "return " + (value == null ? "" : value.toString()) + ";";
    }

    @Override
    public int getRelativeCursorPosition() {
        return value == null ? 7 : -1;
    }
}
