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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a transferable theme for the
 * syntax highlighting of the editor.
 *
 * @author mhahnFr
 * @since 14.01.23
 */
public class JSONTheme implements SPTheme {
    /** The list of the used styles. */
    private List<FStyle> styles = new ArrayList<>();
    /**
     * Maps the {@link TokenType}s to either the index of the
     * {@link FStyle} to be used or another {@link TokenType}
     * whose style is then inherited.
     */
    private Map<TokenType, Object> tokenStyles = new EnumMap<>(TokenType.class);

    @Override
    public FStyle styleFor(TokenType tokenType) {
        final var style = tokenStyles.get(tokenType);
        if (style != null) {
            if (style instanceof TokenType) {
                return styleFor((TokenType) style);
            }
            if ((Integer) style > 0 && (Integer) style < styles.size()) {
                return styles.get((Integer) style);
            }
        }
        return new FStyle();
    }
}
