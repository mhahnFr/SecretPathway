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
import mhahnFr.SecretPathway.gui.editor.theme.json.JSONTheme;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;
import mhahnFr.utils.json.JSONParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a settings window.
 *
 * @author mhahnFr
 * @since 09.01.23
 */
public class SettingsWindow extends JDialog implements DarkModeListener {
    /** The list with all documents enabling their dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    private final Settings settings = Settings.getInstance();
    private JButton themeButton;
    private JComboBox<String> themeBox;

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

            final var themePanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
            themePanel.setBorder(new EtchedBorder());
                final var themeLabel = new DarkComponent<>(new JLabel("The theme used for the editor:"), components).getComponent();

                final var themeBoxPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                    themeBox = new JComboBox<>();
                    themeBox.setEditable(false);

                    themeButton = new JButton("Choose...");
                themeBoxPanel.add(themeBox,    BorderLayout.CENTER);
                themeBoxPanel.add(themeButton, BorderLayout.EAST);
            themePanel.add(themeLabel);
            themePanel.add(themeBoxPanel);
        panel.add(spinnerPanel);
        panel.add(checkBoxes);
        panel.add(themePanel);

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

        themeButton.addActionListener(__ -> themeButtonClick());

        themeBox.addItem(Constants.Editor.DEFAULT_THEME);
        themeBox.addItem(Constants.Editor.CHOOSE_THEME);
        final var themePath = settings.getEditorThemePath();
        if (themePath != null) {
            themeBox.addItem(themePath);
            themeBox.setSelectedItem(themePath);
        } else {
            themeBox.setSelectedItem(Constants.Editor.DEFAULT_THEME);
            themeButton.setVisible(false);
        }
        themeBox.addItemListener(this::themeChanged);
    }

    private void themeChanged(ItemEvent event) {
        final var item = (String) event.getItem();
        switch (item) {
            case Constants.Editor.DEFAULT_THEME: themeButton.setVisible(false); break;

            default:
                if (!tryOpen(new File(item))) {
                    themeBox.removeItem(item);
                }

            case Constants.Editor.CHOOSE_THEME: themeButton.setVisible(true); break;
        }
    }

    private boolean tryOpen(final File file) {
        try (final var is = new BufferedInputStream(new FileInputStream(file))) {
            final var theme = new JSONTheme();
            new JSONParser(new StringStream(new String(is.readAllBytes(), StandardCharsets.UTF_8))).readInto(theme);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read file:\n" + e.getLocalizedMessage(),
                    Constants.NAME + ": File error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void themeButtonClick() {
        final var chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileHidingEnabled(true);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final var file = chooser.getSelectedFile();

            if (tryOpen(file)) {
                final var path = file.getPath();
                themeBox.addItem(path);
                themeBox.setSelectedItem(path);
            }
        }
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
        settings.removeDarkModeListener(this);

        final var selected = themeBox.getSelectedItem();
        if (!Objects.equals(selected, Constants.Editor.CHOOSE_THEME)) {
            settings.setEditorThemePath(Objects.equals(selected, Constants.Editor.DEFAULT_THEME) ? null : (String) selected);
        }

        super.dispose();
    }
}
