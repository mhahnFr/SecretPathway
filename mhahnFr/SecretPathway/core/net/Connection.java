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

import java.io.Closeable;

/**
 * This class represents a connection to a MUD driver.
 *
 * @since 02.11.2022
 * @author mhahnFr
 */
public class Connection implements Closeable, AutoCloseable {
    /** The hostname or the IP address of the endpoint of this connection instance. */
    private final String hostname;
    /** The port on which to connect to the {@link Connection#hostname}.            */
    private final int port;

    /**
     * Constructs a new connection using the given hostname and port.
     *
     * @param hostname the hostname or the IP address to connect to
     * @param port the port on which to connect
     * @throws IllegalArgumentException if the given hostname or port are invalid
     */
    public Connection(String hostname, int port) {
        if (port <= 0 || hostname == null || hostname.isBlank()) {
            throw new IllegalArgumentException("Invalid hostname or port: \"" + hostname + ":" + port + "\"!");
        }
        this.hostname = hostname;
        this.port     = port;
    }

    public String getHostname() { return hostname; }

    public int getPort() { return port; }

    public void establishConnection() {
        // TODO
    }

    public boolean isClosed() {
        // TODO
        return false;
    }

    @Override
    public void close() {
        // TODO
    }

    /**
     * Tries to create a new {@link Connection} instance. Returns {@code null} on error.
     *
     * @param hostname the hostname or the IP address to connect to
     * @param port the port on which to connect to the given endpoint
     * @return a new {@link Connection} instance or {@code null} on error
     */
    public static Connection create(String hostname, Integer port) {
        try {
            return new Connection(hostname, port);
        } catch (Exception e) {
            return null;
        }
    }
}
