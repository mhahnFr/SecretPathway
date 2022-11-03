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

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Instances of this class represent a window in which the user can play a MUD.
 *
 * @since 31.10.2022
 * @author mhahnFr
 */
public class MainWindow extends JFrame {
    /** The connection associated with this window. */
    private final Connection connection;

    /**
     * Constructs a MainWindow. The given connection is used to connect to a MUD if given,
     * otherwise, the last connection is reestablished. If that also fails, the user is
     * prompted to enter the necessary information.
     *
     * @param connection the {@link Connection} instance used as connection
     */
    public MainWindow(Connection connection) {
        super(Constants.NAME);
        this.connection = connection == null ? restoreOrPromptConnection() : connection;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // TODO: GUI setup

        restoreBounds();
    }

    /**
     * Constructs a MainWindow. If the stored previous connection could not be reestablished,
     * the user is prompted for the connection details.
     */
    public MainWindow() {
        this(null);
    }

    @Override
    public void dispose() {
        if (!connection.isClosed()) {
            if (!promptConnectionClosing()) { return; }
            connection.close();
        }
        Settings.getInstance().setHostname(connection.getHostname())
                              .setPort(connection.getPort())
                              .setWindowLocation(getX(), getY())
                              .setWindowSize(getWidth(), getHeight());
        Settings.getInstance().flush();
        super.dispose();
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
        final String hostHint = "hostname or IP address, ex: 127.0.0.1",
                     portHint = "port, ex: 4242";

        var wrapPanel = new JPanel(new BorderLayout());

        var errorLabel = new JLabel();
        errorLabel.setForeground(Color.red);
        errorLabel.setVisible(false);

        var panel     = new JPanel(new GridLayout(2, 1));

        var hostPanel = new JPanel(new GridLayout(2, 1));
        var hostField = new JTextField(hostHint);
        hostField.setForeground(Color.lightGray);
        hostField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (hostField.getText().equals(hostHint)) {
                    hostField.setText("");
                    hostField.setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (hostField.getText().isBlank()) {
                    hostField.setText(hostHint);
                    hostField.setForeground(Color.lightGray);
                }
            }
        });
        hostPanel.add(new JLabel("Enter the hostname or the IP address of the MUD server:"));
        hostPanel.add(hostField);

        var portPanel = new JPanel(new GridLayout(2, 1));
        var portField = new JTextField();
        portField.setText(portHint);
        portField.setForeground(Color.lightGray);
        portField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (portField.getText().equals(portHint)) {
                    portField.setText("");
                    portField.setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (portField.getText().isBlank()) {
                    portField.setText(portHint);
                    portField.setForeground(Color.lightGray);
                }
            }
        });
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
                toReturn = Connection.create(hostField.getText(), Integer.decode(portField.getText()));
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
        Connection toReturn = Connection.create(Settings.getInstance().getHostname(), Settings.getInstance().getPort());
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
