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

import mhahnFr.utils.StreamPosition;

/**
 * This class represents an error as highlighting
 * element. They contain as addition a message.
 *
 * @param <T> the actual type
 * @author mhahnFr
 * @since 04.03.23
 */
public class MessagedHighlight<T extends HighlightType> extends Highlight<T> {
    /** The message for this highlighting element. */
    private final String message;

    /**
     * Constructs this messaged highlighting element.
     *
     * @param begin   the beginning position
     * @param end     the end position
     * @param type    the type
     * @param message the message
     */
    public MessagedHighlight(final int    begin,
                             final int    end,
                             final T      type,
                             final String message) {
        super(begin, end, type);

        this.message = message;
    }

    /**
     * Constructs this messaged highlighting element.
     *
     * @param begin   the beginning position
     * @param end     the end position
     * @param type    the type
     * @param message the message
     */
    public MessagedHighlight(final StreamPosition begin,
                             final StreamPosition end,
                             final T              type,
                             final String         message) {
        this(begin.position(), end.position(), type, message);
    }

    /**
     * Returns the message associated with this
     * highlighting element.
     *
     * @return the associated message
     */
    public String getMessage() {
        return message;
    }
}
