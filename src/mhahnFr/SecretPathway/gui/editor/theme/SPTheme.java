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

package mhahnFr.SecretPathway.gui.editor.theme;

import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.utils.gui.abstraction.FStyle;

/**
 * This interface defines the functionality of a syntax highlighting
 * theme.
 *
 * @author mhahnFr
 * @since 10.01.23
 */
public interface SPTheme {
    /**
     * Generates the {@link FStyle} for the given {@link TokenType}.
     *
     * @param tokenType the token type to be highlighted
     * @return the style used for the highlighting
     */
    FStyle styleFor(final TokenType tokenType);
}
