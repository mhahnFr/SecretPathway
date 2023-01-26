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

package mhahnFr.SecretPathway.core.parser.tokenizer;

import mhahnFr.utils.StreamPosition;

/**
 * This record holds all information related to a specific Token.
 *
 * @param beginPos the beginning of this token in the text
 * @param type the type of this token
 * @param payload the payload held by this token
 * @param endPos the end of this token in the text
 */
public record Token(StreamPosition beginPos, TokenType type, Object payload, StreamPosition endPos) {
    /**
     * Returns the beginning index inside the original text.
     *
     * @return the beginning index
     */
    public int begin() {
        return beginPos.position();
    }

    /**
     * Returns the end index inside the original text.
     *
     * @return the end index
     */
    public int end() {
        return endPos.position();
    }
}
