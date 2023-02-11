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

package mhahnFr.SecretPathway.gui.editor.theme.json;

import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.gui.editor.theme.SPTheme;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.gui.abstraction.FStyle;
import mhahnFr.utils.json.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private List<JSONStyle> styles = new ArrayList<>();
    /**
     * Maps the {@link TokenType}s to either the name of the
     * {@link FStyle} to be used or another {@link TokenType}
     * whose style is then inherited.
     */
    private Map<TokenType, String> tokenStyles = new EnumMap<>(TokenType.class);

    @Override
    public FStyle styleFor(TokenType tokenType) {
        final var style = tokenStyles.get(tokenType);
        if (style != null) {
            // TODO: Cache
            for (final var element : styles) {
                if (element.getName().equals(style)) {
                    return element.getNative();
                }
            }
        }
        return new FStyle();
    }

    /**
     * Reads a {@link JSONTheme} from the given JSON file.
     *
     * @param path the path to the file
     * @return the read theme or {@code null} in case of an error
     */
    public static JSONTheme from(final String path) {
        if (path == null) return null;

        try (final var reader = new BufferedInputStream(new FileInputStream(path))) {
            final var theme = new JSONTheme();
            new JSONParser(new StringStream(new String(reader.readAllBytes(), StandardCharsets.UTF_8))).readInto(theme);
            return theme;
        } catch (Exception __) {
            return null;
        }
    }
}
