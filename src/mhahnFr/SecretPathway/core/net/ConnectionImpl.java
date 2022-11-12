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

package mhahnFr.SecretPathway.core.net;

/**
 * This class contains the connection implementation for Java on desktop
 * computers.
 *
 * @since 12.11.2022
 * @author mhahnFr
 */
public class ConnectionImpl extends Connection {
    /**
     * Constructs this connection representation.
     *
     * @param host the hostname or the IP address to connect to
     * @param port the port number to be used
     */
    public ConnectionImpl(String host, int port) {
        super(host, port);
    }

    @Override
    public void establishConnection() {
        // TODO
    }

    @Override
    public boolean isClosed() {
        // TODO
        return false;
    }

    @Override
    public void close() {
        // TODO
    }
}
