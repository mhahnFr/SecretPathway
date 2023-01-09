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

import mhahnFr.SecretPathway.core.Constants;
import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.net.Connection;

import mhahnFr.SecretPathway.core.net.ConnectionFactory;
import mhahnFr.SecretPathway.gui.helper.MessageReceiver;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkTextComponent;
import mhahnFr.utils.gui.DocumentAdapter;
import mhahnFr.utils.gui.HintTextField;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represent a window in which the user can play a MUD.
 *
 * @since 31.10.2022
 * @author mhahnFr
 */
public class MainWindow extends JFrame implements ActionListener, MessageReceiver {
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

        delegate = new ConnectionDelegate(this.connection, this, mainPane);
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
    @Override
    public void showMessageFrom(Object sender, String message, Color color, int timeout) {
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

            final var checkBoxes = new DarkComponent<>(new JPanel(new GridLayout(3, 1)), components).getComponent();
                final var darkMode = new DarkComponent<>(new JCheckBox("Enable dark mode"), components).getComponent();

                final var editorInlined = new DarkComponent<>(new JCheckBox("Use inlined editor"), components).getComponent();

                final var editorHighlighting = new DarkComponent<>(new JCheckBox("Automatically enable syntax highlighting in the editor"), components).getComponent();
            checkBoxes.add(darkMode);
            checkBoxes.add(editorInlined);
            checkBoxes.add(editorHighlighting);

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
        panel.add(checkBoxes);
        panel.add(connectionPanel);

        hostField.setText(connection.getHostname());
        portField.setText(Integer.toString(connection.getPort()));

        darkMode.setSelected(Settings.getInstance().getDarkMode());
        editorInlined.setSelected(Settings.getInstance().getEditorInlined());
        editorHighlighting.setSelected(Settings.getInstance().getSyntaxHighlighting());

        darkMode.addItemListener(event -> {
            final boolean dark = darkMode.isSelected();

            setDark(dark);

            for (var component : components) {
                component.setDark(dark);
            }

            Settings.getInstance().setDarkMode(dark);
        });
        editorInlined.addItemListener(__ -> Settings.getInstance().setEditorInlined(editorInlined.isSelected()));
        editorHighlighting.addItemListener(__ -> Settings.getInstance().setSyntaxHighlighting(editorHighlighting.isSelected()));

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
                        delegate = new ConnectionDelegate(connection, MainWindow.this, mainPane);
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
            mainPane.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    mainPane.setCaretPosition(mainPane.getText().length());
                }
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
            delegate = new ConnectionDelegate(this.connection, this, mainPane);
            setTitle(Constants.NAME + ": " + this.connection.getName());
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
            delegate = new ConnectionDelegate(connection, this, mainPane);
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
}
