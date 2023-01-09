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
import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.utils.gui.DarkComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a settings window.
 *
 * @author mhahnFr
 * @since 09.01.23
 */
public class SettingsWindow extends JDialog {
    /** The list with all documents enabling their dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();

    /**
     * Constructs this settings window using the given owner.
     *
     * @param owner the owner of this window
     */
    public SettingsWindow(final Frame owner) {
        super(owner, Constants.NAME + ": Settings", true);

        createContent();

        Settings.getInstance().addDarkModeListener(this::setDark);

        setDark(Settings.getInstance().getDarkMode());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    /**
     * Creates the content of this window.
     */
    private void createContent() {
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
        panel.add(spinnerPanel);
        panel.add(checkBoxes);

        getContentPane().add(panel);
        pack();

        final var settings = Settings.getInstance();

        stepper.addChangeListener(__ -> settings.setFontSize((Integer) stepper.getValue()));

        darkMode.setSelected(settings.getDarkMode());
        editorInlined.setSelected(settings.getEditorInlined());
        editorHighlighting.setSelected(settings.getSyntaxHighlighting());

        darkMode.addItemListener(__ -> settings.setDarkMode(darkMode.isSelected()));
        editorInlined.addItemListener(__ -> settings.setEditorInlined(editorInlined.isSelected()));
        editorHighlighting.addItemListener(__ -> settings.setSyntaxHighlighting(editorHighlighting.isSelected()));
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
}
