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
 */
public class Token {
    private final StreamPosition beginPos;
    private final StreamPosition endPos;
    private final TokenType type;
    private final Object payload;

    /**
     * TODO
     *
     * @param beginPos the beginning of this token in the text
     * @param type the type of this token
     * @param payload the payload held by this token
     * @param endPos the end of this token in the text
     */
    public Token(final StreamPosition beginPos,
                 final TokenType      type,
                 final Object         payload,
                 final StreamPosition endPos) {
        this.beginPos = beginPos;
        this.type     = type;
        this.payload  = payload;
        this.endPos   = endPos;
    }

    /**
     * Returns the beginning index inside the original text.
     *
     * @return the beginning index
     */
    public int begin() {
        return beginPos.position();
    }

    public StreamPosition beginPos() {
        return beginPos;
    }

    public TokenType type() {
        return type;
    }

    public Object payload() {
        return payload;
    }

    /**
     * Returns the end index inside the original text.
     *
     * @return the end index
     */
    public int end() {
        return endPos.position();
    }

    public StreamPosition endPos() {
        return endPos;
    }
}
