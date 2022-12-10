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

import mhahnFr.SecretPathway.core.Constants;
import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.net.Connection;

import mhahnFr.SecretPathway.core.net.ConnectionFactory;
import mhahnFr.SecretPathway.core.net.ConnectionListener;
import mhahnFr.utils.ByteHelper;
import mhahnFr.utils.Pair;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkTextComponent;
import mhahnFr.utils.gui.HintTextField;
import mhahnFr.utils.gui.abstraction.FStyle;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Instances of this class represent a window in which the user can play a MUD.
 *
 * @since 31.10.2022
 * @author mhahnFr
 */
public class MainWindow extends JFrame implements ActionListener {
    /** A list with the components that should be capable to become dark. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The connection associated with this window.                       */
    private Connection connection;
    /** The delegate of the connection.                                   */
    private ConnectionDelegate delegate;
    /** The main {@link JTextPane} which contains the incoming text.      */
    private JTextPane mainPane;
    /** The text field for text to be sent.                               */
    private JTextField promptField;
    /** The label for the message overlay.                                */
    private JLabel messageLabel;
    /** The panel with the buttons.                                       */
    private JPanel buttonPanel;
    /** The panel with buttons to be hidden by default.                   */
    private JPanel otherButtons;
    /** The button responsible for expanding invisible buttons.           */
    private JButton expandButton;
    /** The timer for the message overlay.                                */
    private Timer messageTimer;
    /** Indicates whether the dark mode is active.                        */
    private boolean dark;
    /** Indicates whether the additional buttons are currently visible.   */
    private boolean otherButtonsVisible;

    /**
     * Constructs a MainWindow. The given connection is used to connect to a MUD if given,
     * otherwise, the last connection is reestablished. If that also fails, the user is
     * prompted to enter the necessary information.
     *
     * @param connection the {@link Connection} instance used as connection
     */
    public MainWindow(Connection connection) {
        this.connection = connection == null ? restoreOrPromptConnection() : connection;

        setTitle(Constants.NAME + ": " + this.connection.getName());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        createContent();
        createMenuBar();

        restoreBounds();

        setDark(Settings.getInstance().getDarkMode());

        delegate = new ConnectionDelegate(this.connection);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            promptField.requestFocusInWindow();
        }
    }

    /**
     * Displays the given string in the status label. If {@code message} is {@code null},
     * the text is hided. A timer that might be pending is stopped by this method. A new
     * timer is started if the timeout is bigger than zero.
     *
     * @param message the message to be displayed
     * @param color the color to be used to display the message
     * @param timeout the time in milliseconds after which the text is hided
     */
    private void showMessage(String message, Color color, int timeout) {
        if (messageTimer != null) {
            messageTimer.stop();
            messageTimer = null;
        }
        if (message == null) {
            messageLabel.setVisible(false);
        } else {
            messageLabel.setText(message);
            if (color == null) { color = dark ? Color.white : Color.black; }
            messageLabel.setForeground(color);
            messageLabel.setVisible(true);
            if (timeout > 0) {
                messageTimer = new Timer(timeout, __ -> {
                    messageLabel.setVisible(false);
                    this.messageTimer = null;
                });
                messageTimer.setRepeats(false);
                messageTimer.start();
            }
        }
    }

    /**
     * Displays the given string in the status label if the sender is the delegate.
     *
     * @param sender the sender of the message
     * @param message the message to be displayed
     * @param color the color to be used for the message
     * @param timeout the duration in milliseconds to display the message
     * @see #showMessage(String, Color, int)
     */
    private void showMessageFrom(Object sender, String message, Color color, int timeout) {
        if (sender == delegate) {
            showMessage(message, color, timeout);
        }
    }

    /**
     * Sets whether the dark mode should be active.
     *
     * @param dark indicating whether to enable or disable the dark mode
     */
    private void setDark(final boolean dark) {
        this.dark = dark;

        for (var component : components) {
            component.setDark(dark);
        }
    }

    /**
     * Creates the button field for optional, additional buttons.
     */
    private void maybeCreateButtonField() {
        if (expandButton == null) {
            expandButton = new JButton("<");
            expandButton.addActionListener(this);
            expandButton.setActionCommand(Constants.Actions.EXPAND_BUTTONS);

            otherButtons = new DarkComponent<>(new JPanel(), components).getComponent();
            otherButtons.setLayout(new BoxLayout(otherButtons, BoxLayout.X_AXIS));

            buttonPanel.add(expandButton);
            buttonPanel.add(otherButtons);

            otherButtons.setVisible(otherButtonsVisible);
        }
    }

    /**
     * Creates a menu bar for this window. Adds to default menu items as well.
     */
    private void createMenuBar() {
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            Desktop.getDesktop().setQuitHandler((e, response) -> {
                if (!connection.isClosed()) {
                    if (!promptConnectionClosing()) {
                        response.cancelQuit();
                    }
                }
                saveSettings();
                response.performQuit();
            });
        }
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_ABOUT)) {
            Desktop.getDesktop().setAboutHandler(e -> showAboutWindow());
        } else {
            maybeCreateButtonField();

            final var aboutButton = new JButton("About");
            aboutButton.addActionListener(__ -> showAboutWindow());

            otherButtons.add(aboutButton);
        }
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES)) {
            Desktop.getDesktop().setPreferencesHandler(__ -> showSettings());
        } else {
            maybeCreateButtonField();

            final var settingsButton = new JButton("Settings");
            settingsButton.addActionListener(__ -> showSettings());

            otherButtons.add(settingsButton);
        }
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_MENU_BAR)) {
            Desktop.getDesktop().setDefaultMenuBar(generateJMenuBar());
        } else {
            maybeCreateButtonField();

            final var connectionButton = new JButton("Connection");

                final var contextMenu = new JPopupMenu();
                    final var newConnection = new JMenuItem("New...");
                    newConnection.setActionCommand(Constants.Actions.NEW);
                    newConnection.addActionListener(this);

                    final var closeConnection = new JMenuItem("Close");
                    closeConnection.setActionCommand(Constants.Actions.CLOSE);
                    closeConnection.addActionListener(this);

                    final var reconnectConnection = new JMenuItem("Reconnect");
                    reconnectConnection.setActionCommand(Constants.Actions.RECONNECT);
                    reconnectConnection.addActionListener(this);

                contextMenu.add(newConnection);
                contextMenu.add(closeConnection);
                contextMenu.addSeparator();
                contextMenu.add(reconnectConnection);

            connectionButton.setComponentPopupMenu(contextMenu);
            connectionButton.addActionListener(__ -> {
                final var location = connectionButton.getLocation();

                connectionButton.getComponentPopupMenu().show(connectionButton, location.x, location.y + connectionButton.getHeight());
            });

            otherButtons.add(connectionButton);
        }
    }

    /**
     * Creates and returns a menu bar.
     *
     * @return the main menu bar
     */
    private JMenuBar generateJMenuBar() {
        var toReturn = new JMenuBar();

        var connectionMenu = new JMenu("Connection");

            var newItem = new JMenuItem("New...");
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_DOWN_MASK));
            newItem.setActionCommand(Constants.Actions.NEW);
            newItem.addActionListener(this);

            var closeItem = new JMenuItem("Close");
            closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.META_DOWN_MASK));
            closeItem.setActionCommand(Constants.Actions.CLOSE);
            closeItem.addActionListener(this);

            var reconnectItem = new JMenuItem("Reconnect");
            reconnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.META_DOWN_MASK));
            reconnectItem.setActionCommand(Constants.Actions.RECONNECT);
            reconnectItem.addActionListener(this);

        connectionMenu.add(newItem);
        connectionMenu.add(closeItem);
        connectionMenu.addSeparator();
        connectionMenu.add(reconnectItem);

        toReturn.add(connectionMenu);

        return toReturn;
    }

    /**
     * Displays the settings.
     */
    private void showSettings() {
        final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();

        final var window = new JDialog(this, Constants.NAME + ": Settings", true);
        window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        window.setResizable(false);

        final var panel = new DarkComponent<>(new JPanel(), components).getComponent();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            final var spinnerPanel = new DarkComponent<>(new JPanel(), components).getComponent();
            spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));

                final var stepperLabel = new DarkComponent<>(new JLabel("The font size:"), components).getComponent();

                final var stepper = new DarkComponent<>(new JSpinner(), components).getComponent();
                stepper.setValue(Settings.getInstance().getFontSize());

            spinnerPanel.add(stepperLabel);
            spinnerPanel.add(stepper);

            final var darkPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                final var darkMode = new DarkComponent<>(new JCheckBox("Enable dark mode"), components).getComponent();

                final var spacer = new DarkComponent<>(new JPanel(), components).getComponent();

            darkPanel.add(darkMode, BorderLayout.WEST);
            darkPanel.add(spacer, BorderLayout.CENTER);

            final var connectionPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 2)), components).getComponent();
            connectionPanel.setBorder(new BevelBorder(BevelBorder.RAISED));

                final var hostLabel = new DarkComponent<>(new JLabel("The hostname:"), components).getComponent();
                final var hostField = new DarkTextComponent<>(new HintTextField("The hostname or IP-address"), components).getComponent();

                final var portLabel = new DarkComponent<>(new JLabel("The port:"), components).getComponent();
                final var portField = new DarkTextComponent<>(new HintTextField("the port"), components).getComponent();

            connectionPanel.add(hostLabel);
            connectionPanel.add(hostField);
            connectionPanel.add(portLabel);
            connectionPanel.add(portField);

        panel.add(spinnerPanel);
        panel.add(darkPanel);
        panel.add(connectionPanel);

        hostField.setText(connection.getHostname());
        portField.setText(Integer.toString(connection.getPort()));

        darkMode.addItemListener(event -> {
            final boolean dark = darkMode.isSelected();

            setDark(dark);

            for (var component : components) {
                component.setDark(dark);
            }

            Settings.getInstance().setDarkMode(dark);
        });
        if (Settings.getInstance().getDarkMode()) {
            darkMode.setSelected(true);
        }

        stepper.addChangeListener(event -> {
            final int size = (int) stepper.getValue();

            Settings.getInstance().setFontSize(size);

            mainPane.setFont(mainPane.getFont().deriveFont((float) size));
        });

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                final var newHost = hostField.getText();
                final int newPort;
                try {
                    newPort = Integer.decode(portField.getText());
                } catch (NumberFormatException exception) {
                    return;
                }

                final var newConnection = ConnectionFactory.create(hostField.getText(), newPort);

                if (!newHost.equals(connection.getHostname()) || newPort != connection.getPort() && newConnection != null) {
                    if (promptConnectionClosing()) {
                        delegate.closeConnection();
                        connection = newConnection;
                        delegate = new ConnectionDelegate(connection);
                    }
                }
            }
        });

        window.getContentPane().add(panel);
        window.pack();
        window.setLocationRelativeTo(this);
        window.setVisible(true);
    }

    /**
     * Creates and displays a modal About dialog.
     */
    private void showAboutWindow() {
        final var window = new AboutDialog(this, true);

        window.setDark(Settings.getInstance().getDarkMode());
        window.setLocationRelativeTo(this);
        window.setVisible(true);
    }

    /**
     * Creates the content for this window.
     */
    private void createContent() {
        final var panel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            messageLabel = new DarkComponent<>(new JLabel(Constants.NAME + " " + Constants.VERSION, SwingConstants.CENTER), components).getComponent();
            messageLabel.setVisible(false);

                      mainPane   = new DarkTextComponent<>(new JTextPane(), components).getComponent();
            final var scrollPane = new DarkComponent<>(new JScrollPane(mainPane), components).getComponent();
            mainPane.setEditable(false);
            mainPane.setFont(Constants.UI.FONT.deriveFont((float) Settings.getInstance().getFontSize()));
            mainPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    mainPane.setCaretPosition(mainPane.getText().length());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {}

                @Override
                public void changedUpdate(DocumentEvent e) {}
            });

            final var promptPanel = new DarkComponent<>(new JPanel(), components).getComponent();
            promptPanel.setLayout(new BoxLayout(promptPanel, BoxLayout.X_AXIS));
                promptField = new DarkTextComponent<>(new HintTextField("Enter some text..."), components).getComponent();
                promptField.setFont(Constants.UI.FONT);
                promptField.setActionCommand(Constants.Actions.SEND);
                promptField.addActionListener(this);

                buttonPanel = new DarkComponent<>(new JPanel(), components).getComponent();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                    final var sendButton = new JButton("Send");
                    sendButton.setActionCommand(Constants.Actions.SEND);
                    sendButton.addActionListener(this);

                buttonPanel.add(sendButton);

            promptPanel.add(promptField);
            promptPanel.add(buttonPanel);

        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(promptPanel, BorderLayout.SOUTH);

        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        getContentPane().add(panel);

        setMinimumSize  (new Dimension(300, 200));
        setPreferredSize(new Dimension(750, 500));
    }

    /**
     * Sends the text currently in the prompt text field. Clears the text field.
     */
    private void sendText() {
        delegate.send(promptField.getText());
        promptField.setText("");
    }

    /**
     * Expands normally invisible buttons.
     */
    private void expandButtons() {
        otherButtonsVisible = !otherButtonsVisible;
        otherButtons.setVisible(otherButtonsVisible);
        expandButton.setText(otherButtonsVisible ? ">" : "<");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case Constants.Actions.SEND           -> sendText();
            case Constants.Actions.CLOSE          -> maybeCloseConnection();
            case Constants.Actions.RECONNECT      -> maybeReconnect();
            case Constants.Actions.NEW            -> maybeNewConnection();
            case Constants.Actions.EXPAND_BUTTONS -> expandButtons();

            default -> throw new IllegalStateException("Unexpected action command: " + e.getActionCommand());
        }
    }

    @Override
    public void dispose() {
        if (!maybeCloseConnection()) { return; }
        saveSettings();
        super.dispose();
    }

    /**
     * Prompts the user to enter the details of a new connection. If the
     * user does not cancel, he is asked whether to close the maybe running
     * connection. Reconnects using the new connection.
     */
    private void maybeNewConnection() {
        final var connection = promptConnectionNoFail();
        if (connection != null && promptConnectionClosing()) {
            delegate.closeConnection();
            this.connection = connection;
            delegate = new ConnectionDelegate(this.connection);
        }
    }

    /**
     * Asks the user if he wishes to close the connection if one is active.
     *
     * @return whether the connection has been closed
     */
    private boolean maybeCloseConnection() {
        if (!connection.isClosed()) {
            if (!promptConnectionClosing()) { return false; }
            delegate.closeConnection();
        }
        return true;
    }

    /**
     * Asks the user if he wishes to close a probably established connection.
     * Establishes a connection to the same connection again.
     */
    private void maybeReconnect() {
        if (maybeCloseConnection()) {
            connection = ConnectionFactory.create(connection.getHostname(), connection.getPort());
            delegate = new ConnectionDelegate(connection);
        }
    }

    /**
     * Stores the state of the application.
     */
    private void saveSettings() {
        Settings.getInstance().setHostname(connection.getHostname())
                              .setPort(connection.getPort())
                              .setWindowLocation(getX(), getY())
                              .setWindowSize(getWidth(), getHeight())
                              .flush();

    }

    /**
     * Prompts the user to confirm the closing of the connection.
     *
     * @return whether the user wishes to proceed
     */
    private boolean promptConnectionClosing() {
        return JOptionPane.showConfirmDialog(this, "The connection will be closed.\nContinue?",
                Constants.NAME + ": Closing connection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.OK_OPTION;
    }

    /**
     * Prompts the user to enter the details of a connection. If the
     * user cancels the action, {@code null} is returned.
     *
     * @return a valid connection instance or {@code null}
     */
    private Connection promptConnectionNoFail() {
        return promptConnection(false);
    }

    /**
     * Prompts the user to enter the details of a connection. If the
     * user cancels the action, {@link System#exit(int)} is called.
     *
     * @return a valid connection instance
     */
    private Connection promptConnection() {
        return promptConnection(true);
    }

    /**
     * Prompts the user to enter the necessary connection details. Returns a valid connection
     * instance.
     *
     * @param exitOnFail indicates whether to exit if the user cancels the connection
     * @return a valid connection instance created from the details entered by the user or
     * {@code null} if {@code exitOnFail} is set to {@code true}
     */
    private Connection promptConnection(boolean exitOnFail) {

        var wrapPanel = new JPanel(new BorderLayout());

            var errorLabel = new JLabel();
            errorLabel.setForeground(Color.red);
            errorLabel.setVisible(false);

            var panel = new JPanel(new GridLayout(2, 1));

                var hostPanel = new JPanel(new GridLayout(2, 1));
                    var hostField = new HintTextField("hostname or IP address, ex: 127.0.0.1");
                    hostPanel.add(new JLabel("Enter the hostname or the IP address of the MUD server:"));
                    hostPanel.add(hostField);

                var portPanel = new JPanel(new GridLayout(2, 1));
                    var portField = new HintTextField("port, ex: 4242");
                    portPanel.add(new JLabel("Enter the port to be used:"));
                    portPanel.add(portField);

            panel.add(hostPanel);
            panel.add(portPanel);

        wrapPanel.add(errorLabel, BorderLayout.NORTH);
        wrapPanel.add(panel, BorderLayout.CENTER);

        Connection toReturn = null;

        do {
            if (JOptionPane.showConfirmDialog(this, wrapPanel,
                    Constants.NAME + ": New connection", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                if (exitOnFail) { System.exit(0); }
                else return null;
            }
            try {
                toReturn = ConnectionFactory.create(hostField.getText(), Integer.decode(portField.getText()));
                errorLabel.setText("Invalid parameters!");
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid port!");
            }
            errorLabel.setVisible(true);
        } while (toReturn == null);

        return toReturn;
    }

    /**
     * Tries to reconstruct the previous connection. If that fails, the user is prompted to
     * enter the connection details using the method {@link MainWindow#promptConnection()}.
     *
     * @return a valid connection instance
     * @see #promptConnection()
     */
    private Connection restoreOrPromptConnection() {
        Connection toReturn = ConnectionFactory.create(Settings.getInstance().getHostname(), Settings.getInstance().getPort());
        if (toReturn == null) {
            toReturn = promptConnection();
        }
        return toReturn;
    }

    /**
     * Attempts to restore the bounds of this window from the stored state.
     * If this is not possible, the location and the position are set to default
     * values.
     */
    private void restoreBounds() {
        final int width  = Settings.getInstance().getWindowWidth(),
                  height = Settings.getInstance().getWindowHeight(),
                  x      = Settings.getInstance().getWindowLocationX(),
                  y      = Settings.getInstance().getWindowLocationY();

        if (width < 0 || height < 0) {
            pack();
        } else {
            setSize(width, height);
        }

        if (x < 0 || y < 0) {
            setLocationRelativeTo(null);
        } else {
            setLocation(x, y);
        }
    }

    /**
     * An implementation of the {@link ConnectionListener} for this MainWindow.
     *
     * @since 21.11.2022
     * @author mhahnFr
     */
    private class ConnectionDelegate implements ConnectionListener {
        /** The underlying connection to be controlled.                             */
        private final Connection connection;
        /** The default style used by the main text pane.                           */
        private final Style defaultStyle;
        /** The thread pool to be used.                                             */
        private final ExecutorService threads = Executors.newCachedThreadPool();
        /** The future representing the running listening end of the connection.    */
        private Future<?> listenFuture;
        /** A timer triggering reconnection tries if necessary.                     */
        private Timer reconnectTimer;
        /** Indicates whether something has been received on this connection.       */
        private boolean firstReceive = true;
        /** Indicates whether incoming data should be treated as ANSI escape codes. */
        private boolean wasAnsi = false;
        /** The style currently used for incoming data.                             */
        private FStyle current;
        /** A buffer used for escape codes.                                         */
        private final Vector<Byte> buffer = new Vector<>();

        /**
         * Constructs this delegate.
         *
         * @param connection the connection to be controlled by this delegate
         * @throws IllegalArgumentException if the given connection is {@code null}
         */
        ConnectionDelegate(final Connection connection) {
            if (connection == null) throw new IllegalArgumentException("The connection must not be null!");

            this.connection = connection;
            this.connection.setConnectionListener(this);

            defaultStyle = mainPane.getLogicalStyle();
            current = new FStyle();

            showMessageFrom(this, "Connecting...", null, 0);

            listenFuture = threads.submit(connection::establishConnection);
        }

        /**
         * Sends the given text. Appends a newline character to the given text and
         * inserts the text into the main text pane.
         *
         * @param text the text to be sent
         * @see #mainPane
         */
        void send(final String text) {
            final var tmpText = text + '\n';

            final var document = mainPane.getDocument();
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
                EventQueue.invokeLater(() -> showMessageFrom(this, "Connected.", Color.green, 5000));
            }

            var document = mainPane.getDocument();

            var text      = new Vector<Byte>();
            var ansiBegin = 0;
            var byteCount = 0;

            var closedStyles = new Vector<Pair<Integer, FStyle>>();

            for (int i = 0; i < length; ++i) {
                if (data[i] == 0x1B) {
                    wasAnsi   = true;
                    ansiBegin = byteCount;
                    buffer.clear();
                } else if (data[i] == 0x6D && wasAnsi) {
                    wasAnsi = false;

                    var oldCurrent = new FStyle(current, false);
                    if (parseAnsiBuffer(ByteHelper.castToByte(buffer.toArray(new Byte[0])))) {
                        if (ansiBegin != 0 && closedStyles.isEmpty()) {
                            closedStyles.add(new Pair<>(0, new FStyle(oldCurrent, false)));
                        }
                        closedStyles.add(new Pair<>(ansiBegin, new FStyle(current, false)));
                    } else {
                        System.err.println("Error while parsing ANSI escape code!");
                    }
                } else {
                    if (wasAnsi) {
                        buffer.add(data[i]);
                    } else {
                        text.add(data[i]);
                        ++byteCount;
                    }
                }
            }

            var appendix = new String(ByteHelper.castToByte(text.toArray(new Byte[0])), StandardCharsets.UTF_8);
            try {
                if (closedStyles.isEmpty()) {
                    document.insertString(document.getLength(), appendix, current.asStyle(defaultStyle));
                } else {
                    final var factor = byteCount > 0 ? (double) appendix.length() / byteCount : 1;
                    for (int i = 0; i < closedStyles.size(); ++i) {
                        final int len = (i + 1 < closedStyles.size() ? (int) (closedStyles.get(i + 1).getFirst() * factor) : appendix.length());

                        document.insertString(document.getLength(), appendix.substring((int) (closedStyles.get(i).getFirst() * factor), len), closedStyles.get(i).getSecond().asStyle(defaultStyle));
                    }
                }
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handleError(Exception exception) {
            EventQueue.invokeLater(() -> showMessageFrom(this, "Error happened: " + exception.getLocalizedMessage(), Color.red, 0));
            printException(exception);
            maybeRetry();
        }

        @Override
        public void handleEOF(Exception exception) {
            stopTimer();
            EventQueue.invokeLater(() -> showMessageFrom(this, "Connection closed.", Color.yellow, 0));
            printException(exception);
        }
    }
}
