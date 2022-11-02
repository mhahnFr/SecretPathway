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
        this.connection = connection == null ? restoreOrPromptConnection() : connection;

        // TODO: GUI setup
    }

    /**
     * Constructs a MainWindow. If the stored previous connection could not be reestablished,
     * the user is prompted for the connection details.
     */
    public MainWindow() {
        this(null);
    }

    /**
     * Prompts the user to enter the necessary connection details. Returns a valid connection
     * instance.
     *
     * @return a valid connection instance created from the details entered by the user
     */
    private Connection promptConnection() {
        var panel     = new JPanel(new GridLayout(2, 1));

        var hostPanel = new JPanel(new GridLayout(2, 1));
        var hostField = new JTextField(); // TODO: User hint
        hostPanel.add(new JLabel("Enter the hostname or the IP address of the MUD server:"));
        hostPanel.add(hostField);

        var portPanel = new JPanel(new GridLayout(2, 1));
        var portField = new JTextField(); // TODO: User hint
        portPanel.add(new JLabel("Enter the port to be used:"));
        portPanel.add(portField);

        panel.add(hostPanel);
        panel.add(portPanel);

        Connection toReturn = null;

        do {
            if (JOptionPane.showConfirmDialog(this, panel,
                    Constants.NAME + ": New connection", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                System.exit(0);
            }
            // TODO: Make bullet proof
            toReturn = Connection.create(hostField.getText(), Integer.decode(portField.getText()));
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
}
