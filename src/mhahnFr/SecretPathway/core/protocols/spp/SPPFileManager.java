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

package mhahnFr.SecretPathway.core.protocols.spp;

import mhahnFr.SecretPathway.core.lpc.LPCFileManager;
import mhahnFr.SecretPathway.core.net.Connection;

import java.nio.charset.StandardCharsets;

/**
 * This class represents an LPC file manager using the
 * SecretPathwayProtocol (SPP).
 *
 * @author mhahnFr
 * @since 25.03.23
 */
public class SPPFileManager extends LPCFileManager {
    /** The connection used to communicate with the MUD driver. */
    private final Connection connection;

    /**
     * Constructs this file manager using the given {@link Connection}.
     *
     * @param connection the connection used for communicating
     */
    public SPPFileManager(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Sends a message in the SPP.
     *
     * @param message the message to be sent
     */
    private void send(final String message) {
        final var bytes = message.getBytes(StandardCharsets.UTF_8);

        final var sendBytes = new byte[bytes.length + 3];

        sendBytes[0]                    = 0x02;
        System.arraycopy(bytes, 0, sendBytes, 1, bytes.length);
        sendBytes[sendBytes.length - 2] = 0x03;
        sendBytes[sendBytes.length - 1] = (byte) '\n';

        connection.send(sendBytes);
    }

    @Override
    public String load(String fileName) {
        send("file:fetch:" + fileName);

        // TODO: Answer?
        return "inherit;\npublic string name = \"" + fileName + "\";\n" +
               "public string provider = \"SPPFileManager\";";
    }

    @Override
    public void save(String fileName, String content) {
        send("file:store:" + fileName + ":" + content);
        // TODO: Answer?
    }

    @Override
    public boolean canCompile() {
        return true;
    }

    @Override
    public void compile(String fileName) {
        send("file:compile:" + fileName);
    }
}
