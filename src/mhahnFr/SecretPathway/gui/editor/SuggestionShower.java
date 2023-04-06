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

package mhahnFr.SecretPathway.gui.editor;

import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDefinition;
import mhahnFr.SecretPathway.gui.editor.suggestions.SuggestionType;

/**
 * This interface defines how the user can be showered with
 * {@link mhahnFr.SecretPathway.gui.editor.suggestions.Suggestion}s.
 *
 * @author mhahnFr
 * @since 22.03.23
 */
public interface SuggestionShower {
    /**
     * Called when the suggestions should be updated.
     */
    void updateSuggestions();

    void updateSuggestionContext(final SuggestionType type, final ASTTypeDefinition expected);

    /**
     * Called when the shower of suggestions should start.
     */
    void beginSuggestions();

    /**
     * Called when suggestions for super sends should be shown.
     */
    void beginSuperSuggestions();

    /**
     * Called when the shower should be turned off.
     */
    void endSuggestions();
}
