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

package mhahnFr.SecretPathway.gui;

import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.net.Connection;
import mhahnFr.SecretPathway.core.net.ConnectionListener;
import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.Protocol;
import mhahnFr.SecretPathway.core.protocols.ansi.ANSIPlugin;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An implementation of the {@link ConnectionListener}.
 *
 * @since 21.11.2022
 * @author mhahnFr
 */
public class ConnectionDelegate implements ConnectionListener, ConnectionSender {
    /** The style used for user input.                                             */
    private final FStyle inputStyle;
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
    /** Indicates whether the {@link #current current style} has been changed.     */
    private boolean styleChanged = false;
    /** Indicates whether incoming data should be sent to the protocol abstraction.*/
    private boolean wasSpecial = false;
    /** Indicates whether the telnet IAC is escaped.                               */
    private boolean telnetEscape = false;
    /** Indicates whether the last received byte was telnet's IAC command.         */
    private boolean lastWasIAC = false;
    /** Indicates whether the next style begin needs to be shifted by one.         */
    private boolean shiftByOne = false;
    /** The style currently used for incoming data.                                */
    private FStyle current;
    /** The charset used for the encoding of strings.                              */
    private Charset currentCharset = Settings.getInstance().useUTF8() ? StandardCharsets.UTF_8 : StandardCharsets.US_ASCII;
    /** A buffer used for broken unicode characters.                               */
    private final Vector<Byte> unicodeBuffer = new Vector<>();
    /** The SP plugin.                                                             */
    private final SPPPlugin sppPlugin = new SPPPlugin(this);

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

        inputStyle = new FStyle();
        inputStyle.setForeground(Color.gray);
        defaultStyle = this.pane.getLogicalStyle();
        current = new FStyle();
        protocols = new Protocol(this, sppPlugin,
                                       new TelnetPlugin(),
                                       new ANSIPlugin(this));

        receiver.showMessageFrom(this, "Connecting...", null, 0);

        listenFuture = threads.submit(connection::establishConnection);
    }

    @Override
    public void enableSPP() {
        protocols.activateSPP();
    }

    /**
     * Returns the used {@link SPPPlugin}.
     *
     * @return the used {@link SPPPlugin}
     */
    public SPPPlugin getSppPlugin() {
        return sppPlugin;
    }

    /**
     * Sends the given text. Appends a newline character to the given text and
     * inserts the text into the main text pane.
     *
     * @param text    the text to be sent
     * @param pwdMode whether to prevent the text from being displayed
     */
    void send(final String text, final boolean pwdMode) {
        final var document = pane.getDocument();
        try {
            document.insertString(document.getLength(), (pwdMode ? "*".repeat(text.length()) : text) + '\n', inputStyle.asStyle(defaultStyle));
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        send((text + '\n').getBytes(currentCharset));
    }

    /**
     * Returns whether the SecretPathwayProtocol (SPP) is currently
     * active.
     *
     * @return whether the SPP is active
     */
    public boolean isSPPEnabled() {
        return protocols.isSPPActive();
    }

    @Override
    public void setPromptText(String text) {
        EventQueue.invokeLater(() -> ((MainWindow) receiver).setPromptText(text));
    }

    @Override
    public void openEditor(String path, String content) {
        ((MainWindow) receiver).openEditor(path, content);
    }

    @Override
    public void setPasswordMode(boolean enabled) {
        ((MainWindow) receiver).setPasswordModeEnabled(enabled);
    }

    @Override
    public void send(byte[] bytes) {
        threads.execute(() -> connection.send(bytes));
    }

    @Override
    public void startTLS() {
        threads.execute(connection::startTLS);
    }

    @Override
    public void escapeIAC(boolean escape) {
        telnetEscape = escape;
    }

    @Override
    public void setCharset(Charset charset) {
        currentCharset = charset;
    }

    /**
     * Closes the underlying connection and all other active resources this delegate uses.
     */
    void closeConnection() {
        if (reconnectTimer != null) {
            reconnectTimer.stop();
        }
        connection.close();
        listenFuture.cancel(false);
        threads.shutdown();
    }

    /**
     * Starts a timer to reconnect to the connection. Only started if none is running and the connection
     * has never been established.
     */
    private boolean maybeRetry() {
        if (firstReceive && (reconnectTimer == null || !reconnectTimer.isRunning())) {
            reconnectTimer = new Timer(2500, __ -> listenFuture = threads.submit(connection::establishConnection));
            reconnectTimer.start();
        }
        return firstReceive;
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
     * Returns the currently used {@link FStyle}.
     *
     * @return the currently used style
     */
    public FStyle getCurrentStyle() {
        return current;
    }

    /**
     * Sets the given style to be the new style to be used.
     *
     * @param newStyle the new style to be used
     */
    public void setCurrentStyle(final FStyle newStyle) {
        current = newStyle;
        styleChanged = true;
    }

    /**
     * Fixes unicode characters that might not be received entirely.
     *
     * @param text the vector containing the received bytes
     */
    private void fixUnicode(Vector<Byte> text) {
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
                    while (text.size() > index) {
                        unicodeBuffer.add(text.remove(index));
                    }
                }
            } else {
                unicodeBuffer.add(text.remove(text.size() - 1));
            }
        }
    }

    @Override
    public void receive(byte[] data, int length) {
        try {
            receiveImpl(data, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles received data.
     *
     * @param data   the received data
     * @param length the amount of received bytes
     */
    private void receiveImpl(byte[] data, int length) {
        if (firstReceive) {
            stopTimer();
            firstReceive = false;
            EventQueue.invokeLater(() -> receiver.showMessageFrom(this, "Connected.", Color.green, 5000));
        }

        var document  = pane.getDocument();

        var text      = new Vector<>(unicodeBuffer);
        var ansiBegin = 0;
        var charCount = unicodeBuffer.size() > 0 ? 1 : 0;

        var oldStyle  = new FStyle(current, false);

        unicodeBuffer.clear();

        var closedStyles = new Vector<Pair<Integer, FStyle>>();

        for (int i = 0; i < length; ++i) {
            if (telnetEscape) {
                if ((data[i] & 0xff) == 0xff) {
                    if (lastWasIAC) {
                        lastWasIAC = false;
                    } else {
                        lastWasIAC = true;
                        continue;
                    }
                } else if (lastWasIAC) {
                    lastWasIAC = false;
                    wasSpecial = protocols.process((byte) 0xff);
                }
            }
            if (wasSpecial) {
                wasSpecial = protocols.process(data[i]);
            } else {
                wasSpecial = protocols.process(data[i]);

                if (!wasSpecial) {
                    text.add(data[i]);
                    if (((data[i] & 0xff) >> 7) == 0 || ((data[i] & 0xff) >> 6) == 3) {
                        ++charCount;
                    }
                } else {
                    ansiBegin = charCount;
                }
            }
            if (styleChanged) {
                if (ansiBegin != 0 && closedStyles.isEmpty()) {
                    closedStyles.add(new Pair<>(0, new FStyle(oldStyle, false)));
                }
                closedStyles.add(new Pair<>(ansiBegin, new FStyle(current, false)));
                styleChanged = false;
                oldStyle = current;
            }
        }

        fixUnicode(text);

        var appendix = new String(ByteHelper.castToByte(text.toArray(new Byte[0])), currentCharset);
        try {
            if (closedStyles.isEmpty()) {
                document.insertString(document.getLength(), appendix, current.asStyle(defaultStyle));
            } else {
                for (int i = 0; i < closedStyles.size(); ++i) {
                    final var element = closedStyles.get(i);

                    final var wasShift = shiftByOne;
                    shiftByOne = false;

                    int len;
                    if (i + 1 < closedStyles.size()) {
                        len = closedStyles.get(i + 1).getFirst();
                        if (appendix.length() > len && Character.isHighSurrogate(appendix.charAt(len > 0 ? len - 1 : 0))) {
                            ++len;
                            shiftByOne = true;
                        }
                    } else {
                        len = appendix.length();
                    }

                    int begin = element.getFirst();
                    if (wasShift && begin < len) {
                        ++begin;
                    }

                    document.insertString(document.getLength(), appendix.substring(begin, len), element.getSecond().asStyle(defaultStyle));
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
        if (!maybeRetry()) {
            protocols.onConnectionError();
        }
    }

    @Override
    public void handleEOF(Exception exception) {
        stopTimer();
        protocols.onConnectionError();
        EventQueue.invokeLater(() -> receiver.showMessageFrom(this, "Connection closed.", Color.yellow, 0));
        printException(exception);
    }
}
