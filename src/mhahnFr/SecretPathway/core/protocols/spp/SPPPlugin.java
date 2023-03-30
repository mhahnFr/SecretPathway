/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022 - 2023  mhahnFr
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

import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.ProtocolPlugin;
import mhahnFr.utils.ByteHelper;
import mhahnFr.utils.Pair;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class acts as plugin for the SecretPathwayProtocol (SPP).
 *
 * @author mhahnFr
 * @since 19.12.22
 */
public class SPPPlugin implements ProtocolPlugin {
    /** The buffer for a message in the SPP.     */
    private final List<Byte> buffer = new Vector<>(256);
    private final Map<Object, Pair<String, String>> fetchers = new HashMap<>();
    private final ConnectionSender sender;
    /** Indicates whether this plugin is active. */
    private boolean active;

    public SPPPlugin(final ConnectionSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isBegin(byte b) {
        return b == 0x02;
    }

    @Override
    public boolean process(byte b, ConnectionSender sender) {
        if (b == 0x03) {
            processBuffer();
            buffer.clear();
            return false;
        }
        buffer.add(b);
        return true;
    }

    private void setFetchedValue(final String fileName, final Pair<String, String> newValue) {
        for (final var entry : fetchers.entrySet()) {
            if (entry.getValue().getFirst().equals(fileName)) {
                entry.setValue(newValue);
            }
        }
    }

    private void putFetchedFile(final String message) {
        final var index = message.indexOf(':');
        final var fileName = message.substring(0, index);
        final var content = message.substring(index + 1);

        setFetchedValue(fileName, new Pair<>(fileName, content));
    }

    private void putErrorFile(final String message) {
        setFetchedValue(message, null);
    }

    private void handleFileCommand(final String command) {
        final var index = command.indexOf(':');
        final var code = command.substring(0, index);
        final var remainder = command.substring(index + 1);

        switch (code) {
            case "fetch" -> putFetchedFile(remainder);
            case "error" -> putErrorFile(remainder);
        }
    }

    /**
     * Handles the received SPP message.
     */
    private void processBuffer() {
        final var str = new String(ByteHelper.castToByte(buffer.toArray(new Byte[0])), StandardCharsets.UTF_8);

        final var index = str.indexOf(':');
        final var code = str.substring(0, index);
        switch (code) {
            case "promptField" -> {} // TODO
            case "file" -> handleFileCommand(str.substring(index + 1));
        }
    }

    private void send(final String message) {
        final var bytes = message.getBytes(StandardCharsets.UTF_8);

        final var sendBytes = new byte[bytes.length + 3];

        sendBytes[0] = 0x02;
        System.arraycopy(bytes, 0, sendBytes, 1, bytes.length);
        sendBytes[sendBytes.length - 2] = 0x03;
        sendBytes[sendBytes.length - 1] = (byte) '\n';

         sender.send(sendBytes);
    }

    private void registerFetcher(final Object id, final String fileName) {
        fetchers.put(id, new Pair<>(fileName, null));
    }

    private boolean fetcherWaiting(final Object fetcher) {
        return fetchers.get(fetcher).getSecond() == null;
    }

    public String fetchFile(final Object id,
                            final String fileName) {
        send("file:fetch:" + fileName);
        registerFetcher(id, fileName);
        while (fetcherWaiting(id)) {
            Thread.onSpinWait();
        }
        return fetchers.get(id).getSecond();
    }

    public void saveFile(final String fileName,
                         final String content) {
        // TODO
    }

    public void compileFile(final String fileName) {
        // TODO
    }

    /**
     * Returns whether the SecretPathwayProtocol (SPP) is
     * active.
     *
     * @return whether the SPP is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this plugin should be active.
     *
     * @param active whether this plugin is active
     */
    public void setIsActive(final boolean active) {
        this.active = active;
    }
}
