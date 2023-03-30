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
import mhahnFr.SecretPathway.core.lpc.LocalFileManager;
import mhahnFr.SecretPathway.core.net.Connection;

import mhahnFr.SecretPathway.core.net.ConnectionFactory;
import mhahnFr.SecretPathway.core.protocols.spp.SPPFileManager;
import mhahnFr.SecretPathway.gui.editor.EditorView;
import mhahnFr.SecretPathway.gui.editor.EditorWindow;
import mhahnFr.SecretPathway.gui.helper.MessageReceiver;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkTextComponent;
import mhahnFr.utils.gui.DocumentAdapter;
import mhahnFr.utils.gui.HintTextField;
import mhahnFr.utils.gui.menu.MenuFactory;
import mhahnFr.utils.gui.menu.MenuFrame;

import javax.swing.*;
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
public class MainWindow extends MenuFrame implements ActionListener, MessageReceiver {
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
    /** The panel displaying the normal view.                             */
    private JPanel mainPanel;
    private JPanel promptWrapperPanel;
    /** The timer for the message overlay.                                */
    private Timer messageTimer;
    /** Indicates whether the dark mode is active.                        */
    private boolean dark;
    /** Indicates whether the editor is currently inlined.                */
    private boolean editorShowing;
    private boolean passwordMode;

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

        final var settings = Settings.getInstance();

        settings.addDarkModeListener(this::setDark);
        settings.addListener(this::settingsListener);

        setDark(settings.getDarkMode());

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
     * The listening function for settings changes.
     *
     * @param key the key of the changed setting
     * @param newValue the new value of the changed setting
     */
    private void settingsListener(final String key, final Object newValue) {
        if (key.equals(Settings.Keys.FONT_SIZE)) {
            changeFontSize((Integer) newValue);
        }
    }

    /**
     * Changes the font size of the {@link #mainPane} to the
     * given size.
     *
     * @param size the new font size
     */
    private void changeFontSize(final int size) {
        mainPane.setFont(mainPane.getFont().deriveFont((float) size));
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
     * Creates the menu bar.
     */
    private void createMenuBar() {
        final var menuFactory = MenuFactory.getInstance();

        menuFactory.setMenuProvider(new SharedMenuProvider());
        menuFactory.setAboutAction(this::showAboutWindow);
        menuFactory.setSettingsAction(this::showSettings);

        setJMenuBar(MenuFactory.getInstance().createMenuBar(this));
    }

    /**
     * Displays the settings.
     */
    private void showSettings() {
        final var window = new SettingsWindow(this);

        window.setLocationRelativeTo(this);
        window.setVisible(true);
    }

    /**
     * Creates and displays a modal About dialog.
     */
    private void showAboutWindow() {
        final var window = new AboutDialog(this, true);

        window.setLocationRelativeTo(this);
        window.setVisible(true);
    }

    /**
     * Creates the content for this window.
     */
    private void createContent() {
        mainPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
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
                promptWrapperPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                    promptField = new DarkTextComponent<>(new HintTextField("Enter some text..."), components).getComponent();
                    promptField.setFont(Constants.UI.FONT);
                    promptField.setActionCommand(Constants.Actions.SEND);
                    promptField.addActionListener(this);
                promptWrapperPanel.add(promptField, BorderLayout.CENTER);

                final var sendButton = new JButton("Send");
                sendButton.setActionCommand(Constants.Actions.SEND);
                sendButton.addActionListener(this);
            promptPanel.add(promptWrapperPanel);
            promptPanel.add(sendButton);

        mainPanel.add(messageLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(promptPanel, BorderLayout.SOUTH);

        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        getContentPane().add(mainPanel);

        setMinimumSize  (new Dimension(300, 200));
        setPreferredSize(new Dimension(750, 500));
    }

    public void setPasswordModeEnabled(final boolean enabled) {
        passwordMode = enabled;

        final var hadFocus = promptField.hasFocus();

        final JTextField newField;
        if (enabled) {
            newField = new DarkTextComponent<>(new JPasswordField(), components).getComponent();
        } else {
            newField = new DarkTextComponent<>(new HintTextField("Enter something..."), components).getComponent();
        }
        newField.setActionCommand(Constants.Actions.SEND);
        newField.addActionListener(this);
        components.removeIf(c -> c.getComponent() == promptField);
        promptWrapperPanel.remove(promptField);
        promptField = newField;
        promptWrapperPanel.add(promptField);
        validate();

        if (hadFocus) {
            promptField.requestFocusInWindow();
        }
    }

    /**
     * Sends the text currently in the prompt text field. Clears the text field.
     */
    private void sendText() {
        delegate.send(promptField.getText(), passwordMode);
        promptField.setText("");
    }

    /**
     * Opens the editor, according to {@link Settings#getEditorInlined()}
     * either inlined or as a separate window.
     */
    public void openEditor() {
        openEditor(null);
    }

    public void openEditor(final String file) {
        final var manager = delegate.isSPPEnabled() ? new SPPFileManager(delegate.getSppPlugin())
                                                    : new LocalFileManager();

        if (Settings.getInstance().getEditorInlined() && !editorShowing) {
            mainPanel.setVisible(false);

            final var editorView = new EditorView(manager, file);
            editorView.onDispose(view -> {
                getContentPane().remove(view);

                mainPanel.setVisible(true);

                editorShowing = false;
                promptField.requestFocusInWindow();
            });

            getContentPane().add(editorView);
            editorShowing = true;
            validate();
            editorView.requestFocusInWindow();
        } else {
            new EditorWindow(this, manager, file).setVisible(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case Constants.Actions.SEND           -> sendText();
            case Constants.Actions.CLOSE          -> maybeCloseConnection();
            case Constants.Actions.RECONNECT      -> maybeReconnect();
            case Constants.Actions.NEW            -> maybeNewConnection();
            case Constants.Actions.OPEN_EDITOR    -> openEditor();

            default -> throw new IllegalStateException("Unexpected action command: " + e.getActionCommand());
        }
    }

    @Override
    public void dispose() {
        if (!maybeCloseConnection()) { return; }
        saveSettings();
        super.dispose();
        System.exit(0);
    }

    @Override
    protected boolean vetoableDispose() {
        return maybeCloseConnection();
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
        final var secureBox = new JCheckBox("Use SSL/TLS");
        secureBox.setSelected(true);

        wrapPanel.add(errorLabel, BorderLayout.NORTH);
        wrapPanel.add(panel, BorderLayout.CENTER);
        wrapPanel.add(secureBox, BorderLayout.SOUTH);

        Connection toReturn = null;

        do {
            if (JOptionPane.showConfirmDialog(this, wrapPanel,
                    Constants.NAME + ": New connection", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                if (exitOnFail) { System.exit(0); }
                else return null;
            }
            try {
                final var text = hostField.getText();
                final var port = Integer.decode(portField.getText());

                if (secureBox.isSelected()) {
                    toReturn = ConnectionFactory.createSecure(text, port);
                } else {
                    toReturn = ConnectionFactory.create(text, port);
                }
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
