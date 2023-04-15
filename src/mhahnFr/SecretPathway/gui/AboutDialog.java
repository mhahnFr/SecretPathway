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
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This instances of this class act as About window.
 *
 * @author mhahnFr
 * @since 09.12.22
 */
public class AboutDialog extends JDialog implements DarkModeListener {
    /** The list with all components with a dark mode. */
    final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();

    /**
     * Constructs an About window.
     *
     * @param owner the owner of this window
     * @param modal whether this dialog is modal
     */
    public AboutDialog(Frame owner, boolean modal) {
        super(owner, Constants.NAME + ": About", modal);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        createContent();

        Settings.getInstance().addDarkModeListener(this);
        setDark(Settings.getInstance().getDarkMode());
    }

    /**
     * Creates the content of this window.
     */
    private void createContent() {
        final var panel = new DarkComponent<>(new JPanel(new GridLayout(3, 1)), components).getComponent();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            final var topPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
            topPanel.add(new DarkComponent<>(new JLabel("<html><b>The " + Constants.NAME + "</b></html>", SwingConstants.CENTER), components).getComponent());
            topPanel.add(new DarkComponent<>(new JLabel("Version " + Constants.VERSION,                   SwingConstants.CENTER), components).getComponent());

            final var spacer = new DarkComponent<>(new JPanel(), components).getComponent();

            final var bottomPanel = new DarkComponent<>(new JPanel(new GridLayout(3, 1)), components).getComponent();
            bottomPanel.add(new DarkComponent<>(new JLabel("<html>© Copyright 2022 - 2023 (<u>https://www.github.com/mhahnFr</u>)</html>",       SwingConstants.CENTER), components).getComponent());
            bottomPanel.add(new DarkComponent<>(new JLabel("<html>Licensed under the terms of the <b>GPL 3.0</b>.</html>",                       SwingConstants.CENTER), components).getComponent());
            bottomPanel.add(new DarkComponent<>(new JLabel("<html>More information: <u>https://www.github.com/mhahnFr/SecretPathway</u></html>", SwingConstants.CENTER), components).getComponent());

        panel.add(topPanel);
        panel.add(spacer);
        panel.add(bottomPanel);

        getContentPane().add(panel);
        pack();
    }

    /**
     * Toggles the dark mode.
     *
     * @param dark whether to enable the dark mode
     */
    public void setDark(final boolean dark) {
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    @Override
    public void darkModeToggled(boolean dark) {
        setDark(dark);
    }

    @Override
    public void dispose() {
        Settings.getInstance().removeDarkModeListener(this);
        super.dispose();
    }
}
