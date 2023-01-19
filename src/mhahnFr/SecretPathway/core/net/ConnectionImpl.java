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

package mhahnFr.SecretPathway.core.net;

import mhahnFr.utils.Pair;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the connection implementation for Java on desktop
 * computers.
 *
 * @since 12.11.2022
 * @author mhahnFr
 */
public class ConnectionImpl extends Connection {
    /** The underlying socket.                                            */
    private Socket socket;
    /** The input stream associated with the held socket.                 */
    private BufferedInputStream in;
    /** The output stream associated with the held socket.                */
    private BufferedOutputStream out;
    /** Indicates whether this connection has been closed.                */
    private boolean closed;
    /** Indicates whether this connection is using TLS.                   */
    private boolean secure;
    /** A buffer used to buffer incoming data before the listener is set. */
    private final List<Pair<byte[], Integer>> emergencyBuffer = new ArrayList<>();

    /**
     * Constructs this connection representation.
     *
     * @param host the hostname or the IP address to connect to
     * @param port the port number to be used
     */
    public ConnectionImpl(String host, int port) {
        this(host, port, false);
    }

    /**
     * Constructs this connection representation.
     *
     * @param host the hostname or the IP address to connect to
     * @param port the port number to be used
     * @param secure whether to use TLS
     */
    public ConnectionImpl(String host, int port, boolean secure) {
        super(host, port);
        this.secure = secure;
    }

    /**
     * Settles the streams.
     *
     * @throws IOException as thrown by the underlying streams and socket
     */
    private void setupStreams() throws IOException {
        in  = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());

        emergencyBuffer.clear();
    }

    /**
     * Starts reading from the socket. Received content is buffered and flushed
     * into the {@link ConnectionListener}. The reading is stopped once this
     * connection is closed.
     *
     * @see #listener
     * @see #closed
     * @see #close()
     * @see #isClosed()
     */
    private void startReceiving() {
        var buffer = new byte[65536];
        int length;

        while (!closed) {
            try {
                length = in.read(buffer);
                if (listener != null) {
                    listener.receive(buffer, length);
                } else {
                    emergencyBuffer.add(new Pair<>(buffer, length));
                }
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    /**
     * Handles the given exception. If a listener is registered, it is called
     * accordingly to the given exception and state of this connection.
     *
     * @param exception the exception to be handled
     */
    private void handleException(IOException exception) {
        if (listener != null) {
            if (closed) {
                listener.handleEOF(exception);
            } else {
                listener.handleError(exception);
            }
        }
    }

    @Override
    public void startTLS() {
        if (!secure) {
            try {
                socket = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(
                        socket,
                        socket.getInetAddress().getHostAddress(),
                        socket.getPort(),
                        true);
                secure = true;
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    @Override
    public void establishConnection() {
        try {
            if (secure) {
                socket = SSLSocketFactory.getDefault().createSocket(hostname, port);
            } else {
                socket = new Socket(hostname, port);
            }
            setupStreams();
            startReceiving();
        } catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public void setConnectionListener(ConnectionListener listener) {
        super.setConnectionListener(listener);
        if (listener != null && !emergencyBuffer.isEmpty()) {
            for (var bufferPair : emergencyBuffer) {
                listener.receive(bufferPair.getFirst(), bufferPair.getSecond());
            }
        }
    }

    @Override
    public boolean send(byte[] data, int length) {
        try {
            out.write(data, 0, length);
            out.flush();
        } catch (IOException e) {
            handleException(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (!closed) {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                handleException(e);
            }
            closed = true;
        }
    }
}
