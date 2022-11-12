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
public abstract class Connection implements Closeable, AutoCloseable {
    /** The hostname or the IP address of the endpoint of this connection instance. */
    protected final String hostname;
    /** The port on which to connect to the {@link Connection#hostname}.            */
    protected final int port;
    /** The name of the connection. Defaults to hostname : port.                    */
    protected String name;

    /**
     * Constructs a new connection using the given hostname and port.
     *
     * @param hostname the hostname or the IP address to connect to
     * @param port the port on which to connect
     * @throws IllegalArgumentException if the given hostname or port are invalid
     */
    protected Connection(String hostname, int port) {
        if (port <= 0 || hostname == null || hostname.isBlank()) {
            throw new IllegalArgumentException("Invalid hostname or port: \"" + hostname + ":" + port + "\"!");
        }
        this.hostname = hostname;
        this.port     = port;
        this.name     = hostname + ":" + port;
    }

    /**
     * Returns the hostname that is used by this connection instance.
     *
     * @return the associated hostname
     */
    public String getHostname() { return hostname; }

    /**
     * Returns the port that is used by this connection instance.
     *
     * @return the associated port
     */
    public int getPort() { return port; }

    /**
     * Returns the name of the connection.
     *
     * @return the name of the connection
     */
    public String getName() { return name; }

    /**
     * This method establishes a connection to the remote host.
     */
    public abstract void establishConnection();

    /**
     * Returns whether this connection has been closed.
     *
     * @return whether the connection has been closed
     */
    public abstract boolean isClosed();

    @Override
    public abstract void close();
}
