/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022  mhahnFr
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

package mhahnFr.SecretPathway.gui.helper;

import java.awt.Color;

/**
 * This interface defines a function used to broadcast a message.
 *
 * @author mhahnFr
 * @since 10.12.22
 */
public interface MessageReceiver {
    /**
     * Called when a new message should be displayed. Using the sender parameter,
     * the implementing receiver can distinguish between the different sending
     * objects.
     *
     * @param sender the sending object
     * @param message the message to be displayed
     * @param color the colour to be used for the display
     * @param timeout amount of time in milliseconds after which the message should disappear
     */
    void showMessageFrom(Object sender, String message, Color color, int timeout);
}
