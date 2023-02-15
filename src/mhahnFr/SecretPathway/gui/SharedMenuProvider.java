/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2023  mhahnFr
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
import mhahnFr.utils.gui.menu.MenuFactory;
import mhahnFr.utils.gui.menu.MenuFrame;
import mhahnFr.utils.gui.menu.MenuProvider;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class SharedMenuProvider extends MenuProvider {
    private static final int mask = System.getProperty("os.name").toLowerCase().contains("mac") ? KeyEvent.META_DOWN_MASK
                                                                                                : KeyEvent.CTRL_DOWN_MASK;

    private void quitAction() {
        if (MenuFrame.vetoableCloseAll()) {
            System.exit(0);
        }
    }

    @Override
    public JMenuBar generateMenuBar(ActionListener listener) {
        final var factory = MenuFactory.getInstance();

        final var toReturn = new JMenuBar();

        final JMenu mainMenu;
        if (!factory.hasMainAbout() || !factory.hasMainQuit() || !factory.hasMainSettings()) {
            mainMenu = new JMenu(Constants.NAME);
            if (!factory.hasMainAbout()) {
                final var about = new JMenuItem("About " + Constants.NAME);
                about.addActionListener(__ -> defaultAboutAction());
                mainMenu.add(about);
                mainMenu.addSeparator();
            }
            if (!factory.hasMainSettings()) {
                final var settings = new JMenuItem("Settings...");
                settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, mask));
                settings.addActionListener(__ -> defaultSettingsAction());
                mainMenu.add(settings);
            }
            if (!factory.hasMainQuit()) {
                final var quit = new JMenuItem("Quit " + Constants.NAME);
                quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
                quit.addActionListener(__ -> quitAction());
                mainMenu.add(quit);
            }
        } else {
            mainMenu = null;
        }

        final var connectionMenu = new JMenu("Connection");
            final var newItem = new JMenuItem("New...");
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
            newItem.setActionCommand(Constants.Actions.NEW);
            newItem.addActionListener(listener);

            final var closeItem = new JMenuItem("Close");
            closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, mask));
            closeItem.setActionCommand(Constants.Actions.CLOSE);
            closeItem.addActionListener(listener);

            final var reconnectItem = new JMenuItem("Reconnect");
            reconnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
            reconnectItem.setActionCommand(Constants.Actions.RECONNECT);
            reconnectItem.addActionListener(listener);
        connectionMenu.add(newItem);
        connectionMenu.add(closeItem);
        connectionMenu.addSeparator();
        connectionMenu.add(reconnectItem);

        final var windowMenu = new JMenu("Window");
            final var openEditorItem = new JMenuItem("LPC Editor");
            openEditorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, mask));
            openEditorItem.setActionCommand(Constants.Actions.OPEN_EDITOR);
            openEditorItem.addActionListener(listener);
        windowMenu.add(openEditorItem);

        if (mainMenu != null) {
            toReturn.add(mainMenu);
        }
        toReturn.add(connectionMenu);
        toReturn.add(windowMenu);

        return toReturn;
    }
}
