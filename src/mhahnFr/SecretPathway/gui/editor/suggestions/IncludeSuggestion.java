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

/**
 * This class represents an {@code #include} statement.
 *
 * @author mhahnFr
 * @since 22.03.23
 */
public class IncludeSuggestion extends StatementSuggestion {
    @Override
    public String getSuggestion() {
        return "#include \"\"";
    }

    @Override
    public String getDescription() {
        return "include";
    }

    @Override
    public int getRelativeCursorPosition() {
        return 10;
    }
}
