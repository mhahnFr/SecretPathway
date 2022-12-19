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

package mhahnFr.SecretPathway.gui;

import mhahnFr.SecretPathway.core.net.Connection;
import mhahnFr.SecretPathway.core.net.ConnectionListener;
import mhahnFr.SecretPathway.core.protocols.Protocol;
import mhahnFr.SecretPathway.core.protocols.spp.SPPPlugin;
import mhahnFr.SecretPathway.core.protocols.telnet.TelnetPlugin;
import mhahnFr.SecretPathway.gui.helper.MessageReceiver;
import mhahnFr.utils.ByteHelper;
import mhahnFr.utils.Pair;
import mhahnFr.utils.gui.abstraction.FStyle;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An implementation of the {@link ConnectionListener} for this MainWindow.
 *
 * @since 21.11.2022
 * @author mhahnFr
 */
class ConnectionDelegate implements ConnectionListener {
    /** The underlying connection to be controlled.                                */
    private final Connection connection;
    /** The default style used by the main text pane.                              */
    private final Style defaultStyle;
    /** The text pane used to write the output.                                    */
    private final JTextPane pane;
    /** The receiver of messages to be displayed for a specified amount of time.   */
    private final MessageReceiver receiver;
    /** The protocol abstraction.                                                  */
    private final Protocol protocols;
    /** The thread pool to be used.                                                */
    private final ExecutorService threads = Executors.newCachedThreadPool();
    /** The future representing the running listening end of the connection.       */
    private Future<?> listenFuture;
    /** A timer triggering reconnection tries if necessary.                        */
    private Timer reconnectTimer;
    /** Indicates whether something has been received on this connection.          */
    private boolean firstReceive = true;
    /** Indicates whether incoming data should be treated as ANSI escape codes.    */
    private boolean wasAnsi = false;
    /** Indicates whether incoming data should be sent to the protocol abstraction.*/
    private boolean wasSpecial = false;
    /** The style currently used for incoming data.                                */
    private FStyle current;
    /** A buffer used for ANSI escape codes.                                       */
    private final Vector<Byte> ansiBuffer = new Vector<>();
    /** A buffer used for broken unicode characters.                               */
    private final Vector<Byte> unicodeBuffer = new Vector<>();

    /**
     * Constructs this delegate.
     *
     * @param connection the connection to be controlled by this delegate
     * @throws IllegalArgumentException if the given connection is {@code null}
     */
    ConnectionDelegate(final Connection connection, final MessageReceiver receiver, final JTextPane pane) {
        if (connection == null) throw new IllegalArgumentException("The connection must not be null!");
        if (receiver   == null) throw new IllegalArgumentException("The message receiver must not be null!");
        if (pane       == null) throw new IllegalArgumentException("The text-pane must not be null!");

        this.connection = connection;
        this.receiver   = receiver;
        this.pane       = pane;

        this.connection.setConnectionListener(this);

        defaultStyle = this.pane.getLogicalStyle();
        current = new FStyle();
        protocols = new Protocol(null, new SPPPlugin(),
                                       new TelnetPlugin());

        receiver.showMessageFrom(this, "Connecting...", null, 0);

        listenFuture = threads.submit(connection::establishConnection);
    }

    /**
     * Sends the given text. Appends a newline character to the given text and
     * inserts the text into the main text pane.
     *
     * @param text the text to be sent
     */
    void send(final String text) {
        final var tmpText = text + '\n';

        final var document = pane.getDocument();
        try {
            document.insertString(document.getLength(), tmpText, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        threads.execute(() -> connection.send(tmpText.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Closes the underlying connection and all other active resources this delegate uses.
     */
    void closeConnection() {
        connection.close();
        listenFuture.cancel(false);
        threads.shutdown();
    }

    /**
     * Starts a timer to reconnect to the connection. Only started if none is running and the connection
     * has never been established.
     */
    private void maybeRetry() {
        if (firstReceive && (reconnectTimer == null || !reconnectTimer.isRunning())) {
            reconnectTimer = new Timer(2500, __ -> listenFuture = threads.submit(connection::establishConnection));
            reconnectTimer.start();
        }
    }

    /**
     * Stops the reconnection timer if one is running.
     */
    private void stopTimer() {
        if (reconnectTimer != null) {
            reconnectTimer.stop();
            reconnectTimer = null;
        }
    }

    /**
     * Prints the stack trace of the given exception. Indicates that it has been handled.
     *
     * @param exception the exception to print
     */
    private void printException(Exception exception) {
        System.err.println("Handled error:");
        exception.printStackTrace();
        System.err.println("--------------");
    }

    /**
     * Parses the contents of the given ANSI buffer. If the buffer could not be parsed
     * properly, {@code false} is returned and the {@link #current current style} is
     * left untouched.
     *
     * @param buffer the ANSI buffer to parse
     * @return whether the buffer was parsed successfully
     */
    private boolean parseAnsiBuffer(byte[] buffer) {
        var str = new String(buffer, StandardCharsets.US_ASCII);

        final var before = new FStyle(current, false);

        try {
            str = str.substring(1);
            final var splits = str.split(";");
            for (int i = 0; i < splits.length; ++i) {
                switch (Integer.parseInt(splits[i])) {
                    case 0  -> current = new FStyle();
                    case 1  -> current.setBold(true);
                    case 3  -> current.setItalic(true);
                    case 4  -> current.setUnderlined(true);
                    case 21 -> current.setBold(false);
                    case 23 -> current.setItalic(false);
                    case 24 -> current.setUnderlined(false);

                    // Foreground
                    case 30 -> current.setForeground(Color.black);
                    case 31 -> current.setForeground(Color.red);
                    case 32 -> current.setForeground(Color.green);
                    case 33 -> current.setForeground(Color.yellow);
                    case 34 -> current.setForeground(Color.blue);
                    case 35 -> current.setForeground(Color.magenta);
                    case 36 -> current.setForeground(Color.cyan);
                    case 37 -> current.setForeground(Color.lightGray);
                    case 39 -> current.setForeground(null);
                    case 90 -> current.setForeground(Color.darkGray);
                    case 97 -> current.setForeground(Color.white);

                    // Background
                    case 40  -> current.setBackground(Color.black);
                    case 41  -> current.setBackground(Color.red);
                    case 42  -> current.setBackground(Color.green);
                    case 43  -> current.setBackground(Color.yellow);
                    case 44  -> current.setBackground(Color.blue);
                    case 45  -> current.setBackground(Color.magenta);
                    case 46  -> current.setBackground(Color.cyan);
                    case 47  -> current.setBackground(Color.lightGray);
                    case 49  -> current.setBackground(null);
                    case 100 -> current.setBackground(Color.darkGray);
                    case 107 -> current.setBackground(Color.white);

                    case 38 -> {
                        ++i;

                        final var code = Integer.parseInt(splits[i]);
                        if (code == 5) {
                            System.err.println("256 bit colours not supported!");
                        } else if (code == 2) {
                            current.setForeground(new Color(Integer.parseInt(splits[i + 1]), Integer.parseInt(splits[i + 2]), Integer.parseInt(splits[i + 3])));
                            i += 3;
                        }
                    }

                    case 48 -> {
                        ++i;

                        final var code = Integer.parseInt(splits[i]);
                        if (code == 5) {
                            System.err.println("256 bit colours not supported!");
                        } else if (code == 2) {
                            current.setBackground(new Color(Integer.parseInt(splits[i + 1]), Integer.parseInt(splits[i + 2]), Integer.parseInt(splits[i + 3])));
                            i += 3;
                        }
                    }

                    default -> System.err.println("Code not supported: " + splits[i] + "!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            current = before;
            return false;
        }
        return true;
    }

    @Override
    public void receive(byte[] data, int length) {
        if (firstReceive) {
            stopTimer();
            firstReceive = false;
            EventQueue.invokeLater(() -> receiver.showMessageFrom(this, "Connected.", Color.green, 5000));
        }

        var document = pane.getDocument();

        var text      = new Vector<>(unicodeBuffer);
        var ansiBegin = 0;
        var charCount = unicodeBuffer.size() > 0 ? 1 : 0;

        unicodeBuffer.clear();

        var closedStyles = new Vector<Pair<Integer, FStyle>>();

        for (int i = 0; i < length; ++i) {
            if (data[i] == 0x1B) {
                wasAnsi   = true;
                ansiBegin = charCount;
                ansiBuffer.clear();
            } else if (data[i] == 0x6D && wasAnsi) {
                wasAnsi = false;

                var oldCurrent = new FStyle(current, false);
                if (parseAnsiBuffer(ByteHelper.castToByte(ansiBuffer.toArray(new Byte[0])))) {
                    if (ansiBegin != 0 && closedStyles.isEmpty()) {
                        closedStyles.add(new Pair<>(0, new FStyle(oldCurrent, false)));
                    }
                    closedStyles.add(new Pair<>(ansiBegin, new FStyle(current, false)));
                } else {
                    System.err.println("Error while parsing ANSI escape code!");
                }
            } else {
                if (wasAnsi) {
                    ansiBuffer.add(data[i]);
                } else {
                    wasSpecial = protocols.process(data[i]);

                    if (!wasSpecial) {
                        text.add(data[i]);
                        if (((data[i] & 0xff) >> 7) == 0 || ((data[i] & 0xff) >> 6) == 3) {
                            ++charCount;
                        }
                    }
                }
            }
        }

        if (!text.isEmpty() && ((text.lastElement() & 0xff) >> 7) == 1) {
            if (((text.lastElement() & 0xff) >> 6) == 2) {
                final var exCount = text.size();

                var index = exCount - 2;
                while (((text.elementAt(index) & 0xff) >> 7) == 1 && ((text.elementAt(index) & 0xff) >> 6) == 2) {
                    --index;
                }

                final var shifted = (text.elementAt(index) & 0xff) >> 4;
                final var oneCount = shifted == 0b1100 ? 2
                                        : (shifted == 0b1110) ? 3 : 4;

                if (text.size() - index < oneCount) {
                    for (int i = 0; i < oneCount - 1; ++i) {
                        unicodeBuffer.add(text.remove(index));
                    }
                }
            } else {
                unicodeBuffer.add(text.remove(text.size() - 1));
            }
        }

        var appendix = new String(ByteHelper.castToByte(text.toArray(new Byte[0])), StandardCharsets.UTF_8);
        try {
            if (closedStyles.isEmpty()) {
                document.insertString(document.getLength(), appendix, current.asStyle(defaultStyle));
            } else {
                for (int i = 0; i < closedStyles.size(); ++i) {
                    final var element = closedStyles.get(i);

                    final int len = i + 1 < closedStyles.size() ? closedStyles.get(i + 1).getFirst() : appendix.length();

                    document.insertString(document.getLength(), appendix.substring(element.getFirst(), len), element.getSecond().asStyle(defaultStyle));
                }
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleError(Exception exception) {
        EventQueue.invokeLater(() -> receiver.showMessageFrom(this, "Error happened: " + exception.getLocalizedMessage(), Color.red, 0));
        printException(exception);
        maybeRetry();
    }

    @Override
    public void handleEOF(Exception exception) {
        stopTimer();
        EventQueue.invokeLater(() -> receiver.showMessageFrom(this, "Connection closed.", Color.yellow, 0));
        printException(exception);
    }
}
