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

package mhahnFr.SecretPathway.core.protocols;

import mhahnFr.SecretPathway.core.net.ConnectionSender;

/**
 * This interface defines the functionality a protocol plugin has
 * to fulfil.
 *
 * @author mhahnFr
 * @since 17.12.22
 */
public interface ProtocolPlugin {
    /**
     * Returns whether the given byte is to be interpreted as
     * the beginning of a sequence this plugin handles.
     * It is called when the state machine tries to determine
     * the type of the received byte. If this function returns
     * {@code true}, incoming data is sent to this plugin's
     * {@link #process(byte, ConnectionSender)} function.
     *
     * @param b the byte that might be the beginning byte
     * @return whether this plugin should be called for processing the next byte
     */
    boolean isBegin(byte b);

    /**
     * Called when a new byte is received and this plugin has
     * indicated that it is responsible for handling the next
     * incoming data. It will return whether the next incoming
     * byte belongs to this plugin.
     *
     * @param b the byte that was received
     * @param sender a reference to the sender responsible for sending back a potential response
     * @return whether this plugin should be called for the next incoming byte
     */
    boolean process(byte b, ConnectionSender sender);
}
