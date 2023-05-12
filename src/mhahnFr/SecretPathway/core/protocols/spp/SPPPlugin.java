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
    /** The buffer for a message in the SPP.                                */
    private final List<Byte> buffer = new Vector<>(256);
    /** A map with all registered fetchers that are waiting for a response. */
    private final Map<Object, Pair<String, String>> fetchers = new HashMap<>(); // FIXME: Multithreading?
    /** The sender used for sending messages. */
    private final ConnectionSender sender;
    /** Indicates whether this plugin is active.                            */
    private boolean active;
    private boolean connectionError;

    /**
     * Constructs this plugin using the given sender.
     *
     * @param sender the sender used for sending messages
     */
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

    @Override
    public void onConnectionError() {
        connectionError = true;
        fetchers.clear();
    }

    /**
     * Updates the result of a fetching.
     *
     * @param fileName the requested file
     * @param newValue the result
     */
    private void setFetchedValue(final String fileName, final Pair<String, String> newValue) {
        for (final var entry : fetchers.entrySet()) {
            if (entry.getValue().getFirst().equals(fileName)) {
                entry.setValue(newValue);
            }
        }
    }

    /**
     * Updates a fetched file.
     *
     * @param message the SPP message
     */
    private void putFetchedFile(final String message) {
        final var index = message.indexOf(':');
        final var fileName = message.substring(0, index);
        final var content = message.substring(index + 1);

        setFetchedValue(fileName, new Pair<>(fileName, content));
    }

    /**
     * Updates a fetched file as an error.
     *
     * @param message the SPP message
     */
    private void putErrorFile(final String message) {
        setFetchedValue(message, null);
    }

    /**
     * Handles an incoming file command of the SPP.
     *
     * @param command the command
     */
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
     * Handles a prompt command of the SPP.
     *
     * @param command the command
     */
    private void handlePromptCommand(final String command) {
        switch (command) {
            case "normal"   -> sender.setPasswordMode(false);
            case "password" -> sender.setPasswordMode(true);
        }
    }

    /**
     * Handles the received SPP message.
     */
    private void processBuffer() {
        final var str = new String(ByteHelper.castToByte(buffer.toArray(new Byte[0])), StandardCharsets.UTF_8);

        final var index = str.indexOf(':');

        final String code      = str.substring(0, index),
                     remainder = str.substring(index + 1);

        switch (code) {
            case "promptField" -> handlePromptCommand(remainder);
            case "prompt"      -> sender.setPromptText(remainder.isEmpty() ? null : remainder);
            case "file"        -> handleFileCommand(remainder);
            case "editor"      -> handleEditorCommand(remainder);
        }
    }

    private void handleEditorCommand(final String message) {
        final var index = message.indexOf(':');

        final String path    = message.substring(0, index),
                     content = message.substring(index + 1);

        sender.openEditor(path, content);
    }

    /**
     * Sends the given message in the SPP.
     *
     * @param message the message to be sent
     */
    private void send(final String message) {
        final var bytes = message.getBytes(StandardCharsets.UTF_8);

        final var sendBytes = new byte[bytes.length + 3];

        sendBytes[0] = 0x02;
        System.arraycopy(bytes, 0, sendBytes, 1, bytes.length);
        sendBytes[sendBytes.length - 2] = 0x03;
        sendBytes[sendBytes.length - 1] = (byte) '\n';

         sender.send(sendBytes);
    }

    /**
     * Registers a fetcher for the given file name.
     *
     * @param id       the ID of the fetcher
     * @param fileName the awaited file
     */
    private void registerFetcher(final Object id, final String fileName) {
        fetchers.put(id, new Pair<>(fileName, null));
    }

    /**
     * Returns whether the given fetcher needs to wait. If
     * an error happens, the fetcher will be unregistered,
     * the {@link Exception} is then rethrown.
     *
     * @param fetcher the fetcher
     * @return whether the fetcher still has to wait
     * @see #unregisterFetcher(Object)
     */
    private boolean fetcherWaiting(final Object fetcher) {
        try {
            return fetchers.get(fetcher).getSecond() == null;
        } catch (Exception e) {
           unregisterFetcher(fetcher);
           throw e;
        }
    }

    /**
     * Unregisters the given fetcher.
     *
     * @param fetcher the fetcher to be unregistered
     */
    private void unregisterFetcher(final Object fetcher) {
        fetchers.remove(fetcher);
    }

    /**
     * Fetches the given file. The current thread is blocked
     * until a response is received.
     *
     * @param id       the ID of the fetcher
     * @param fileName the file to be fetched
     * @return the fetched file
     */
    public String fetchFile(final Object id,
                            final String fileName,
                            final String referrer) {
        if (connectionError) return null;

        send("file:fetch:" + fileName + ":" + (referrer == null ? "/" : referrer));
        registerFetcher(id, fileName);
        while (fetcherWaiting(id)) {
            Thread.onSpinWait();
        }
        final var result = fetchers.get(id).getSecond();
        unregisterFetcher(id);
        return result;
    }

    /**
     * Saves the given file with the given content.
     *
     * @param fileName the file to be saved
     * @param content  the new content of the file
     */
    public void saveFile(final String fileName,
                         final String content) {
        send("file:store:" + fileName + ":" + content);
    }

    /**
     * Compiles the given file.
     *
     * @param fileName the file to be compiled
     */
    public void compileFile(final String fileName) {
        send("file:compile:" + fileName);
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
