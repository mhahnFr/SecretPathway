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
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkTextComponent;
import mhahnFr.utils.gui.HintTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Instances of this class represent a window in which the user can play a MUD.
 *
 * @since 31.10.2022
 * @author mhahnFr
 */
public class MainWindow extends JFrame implements ActionListener {
    /** The connection associated with this window.                       */
    private final Connection connection;
    /** The delegate of the connection.                                   */
    private final ConnectionDelegate delegate;
    /** A list with the components that should be capable to become dark. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The main {@link JTextPane} which contains the incoming text.      */
    private JTextPane mainPane;
    /** The text field for text to be sent.                               */
    private JTextField promptField;

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

        createMenuBar();
        createContent();

        restoreBounds();

        delegate = new ConnectionDelegate(this.connection);
    }

    /**
     * Constructs a MainWindow. If the stored previous connection could not be reestablished,
     * the user is prompted for the connection details.
     */
    public MainWindow() {
        this(null);
    }

    /**
     * Sets whether the dark mode should be active.
     *
     * @param dark indicating whether to enable or disable the dark mode
     */
    private void setDark(final boolean dark) {
        for (var component : components) {
            component.setDark(dark);
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
        }
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES)) {
            Desktop.getDesktop().setPreferencesHandler(__ -> showSettings());
        }
    }

    /**
     * Displays the settings.
     */
    private void showSettings() {
        final var window = new JDialog(this, Constants.NAME + ": Settings", true);
        window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        window.setResizable(false);

        final var panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            final var spinnerPanel = new JPanel();
            spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));

                final var stepperLabel = new JLabel("The font size:");

                final var stepper = new JSpinner();
                stepper.setValue(Settings.getInstance().getFontSize());

            spinnerPanel.add(stepperLabel);
            spinnerPanel.add(stepper);

            final var darkMode = new JCheckBox("Enable dark mode");

        panel.add(spinnerPanel);
        panel.add(darkMode);

        window.getContentPane().add(panel);
        window.pack();
        window.setLocationRelativeTo(this);
        window.setVisible(true);
    }

    /**
     * Creates and displays a modal About dialog.
     */
    private void showAboutWindow() {
        final var window = new JDialog(this, Constants.NAME + ": About", true);
        window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final var panel = new JPanel(new GridLayout(3, 1));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            final var topPanel = new JPanel(new GridLayout(2, 1));
                topPanel.add(new JLabel("<html><b>The " + Constants.NAME + "</b></html>", SwingConstants.CENTER));
                topPanel.add(new JLabel("Version " + Constants.VERSION,                   SwingConstants.CENTER));

            final var spacer = new JPanel();

            final var bottomPanel = new JPanel(new GridLayout(3, 1));
                bottomPanel.add(new JLabel("<html>© Copyright 2022 (<u>https://www.github.com/mhahnFr</u>)</html>",              SwingConstants.CENTER));
                bottomPanel.add(new JLabel("<html>Licensed under the terms of the <b>GPL 3.0</b>.</html>",                       SwingConstants.CENTER));
                bottomPanel.add(new JLabel("<html>More information: <u>https://www.github.com/mhahnFr/SecretPathway</u></html>", SwingConstants.CENTER));

        panel.add(topPanel);
        panel.add(spacer);
        panel.add(bottomPanel);

        window.getContentPane().add(panel);
        window.pack();
        window.setLocationRelativeTo(this);
        window.setVisible(true);
    }

    /**
     * Creates the content for this window.
     */
    private void createContent() {
        final var panel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            final var statusLabel = new DarkComponent<>(new JLabel("Is it connected?", SwingConstants.CENTER), components).getComponent();

                      mainPane   = new DarkTextComponent<>(new JTextPane(), components).getComponent();
            final var scrollPane = new DarkComponent<>(new JScrollPane(mainPane), components).getComponent();
            mainPane.setEditable(false);
            mainPane.setFont(Constants.UI.FONT);
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

                final var sendButton = new JButton("Send");
                sendButton.setActionCommand(Constants.Actions.SEND);
                sendButton.addActionListener(this);

            promptPanel.add(promptField);
            promptPanel.add(sendButton);

        panel.add(statusLabel, BorderLayout.NORTH);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case Constants.Actions.SEND -> sendText();

            default -> throw new IllegalStateException("Unexpected action command: " + e.getActionCommand());
        }
    }

    @Override
    public void dispose() {
        if (!connection.isClosed()) {
            if (!promptConnectionClosing()) { return; }
            connection.close();
        }
        saveSettings();
        super.dispose();
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
                Constants.NAME + ": Closing connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.OK_OPTION;
    }

    /**
     * Prompts the user to enter the necessary connection details. Returns a valid connection
     * instance.
     *
     * @return a valid connection instance created from the details entered by the user
     */
    private Connection promptConnection() {

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
                System.exit(0);
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
        /** The underlying connection to be controlled.                          */
        private final Connection connection;
        /** The future representing the running listening end of the connection. */
        private final Future<?> listenFuture;
        /** The thread pool to be used.                                          */
        private final ExecutorService threads = Executors.newCachedThreadPool();

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

        @Override
        public void receive(byte[] data, int length) {
            var document = mainPane.getDocument();
            try {
                document.insertString(document.getLength(), new String(data, 0, length, StandardCharsets.UTF_8), null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handleError(Exception exception) {
            exception.printStackTrace();
        }
    }
}
