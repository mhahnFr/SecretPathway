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
 * This class acts as abstraction bridge between the general usable class {@link Connection}
 * and the possible implementations.
 *
 * @since 12.11.2022
 * @author mhahnFr
 */
public abstract class ConnectionFactory {
    /**
     * Tries to create a new {@link Connection} instance. Returns {@code null} on error.
     *
     * @param hostname the hostname or the IP address to connect to
     * @param port the port on which to connect to the given endpoint
     * @return a new {@link Connection} instance or {@code null} on error
     */
    public static Connection create(String hostname, Integer port) {
        try {
            return new ConnectionImpl(hostname, port);
        } catch (Exception e) {
            return null;
        }
    }
}
