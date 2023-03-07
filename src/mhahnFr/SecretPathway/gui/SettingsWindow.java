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
import mhahnFr.SecretPathway.gui.editor.theme.SPTheme;
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
    /** The list with all documents enabling their dark mode.     */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** Convenience reference to the settings object.             */
    private final Settings settings = Settings.getInstance();
    /** The button used to choose a theme file.                   */
    private JButton themeButton;
    /** The combo box used for choosing the theme.                */
    private JComboBox<String> themeBox;
    /** The last {@link SPTheme} that has been successfully read. */
    private SPTheme lastRead;

    /**
     * Constructs this settings window using the given owner.
     *
     * @param owner the owner of this window
     */
    public SettingsWindow(final Frame owner) {
        super(owner, Constants.NAME + ": Settings", true);

        createContent();

        Settings.getInstance().addDarkModeListener(this);

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

            final var appearancePanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                final var appearanceLabel = new DarkComponent<>(new JLabel("Appearance:"), components).getComponent();

                final var appearanceBox = new JComboBox<String>();
                appearanceBox.setEditable(false);
            appearancePanel.add(appearanceLabel, BorderLayout.WEST);
            appearancePanel.add(appearanceBox, BorderLayout.CENTER);

            final var checkBoxes = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
                final var editorInlined = new DarkComponent<>(new JCheckBox("Use inlined editor"), components).getComponent();

                final var editorHighlighting = new DarkComponent<>(new JCheckBox("Automatically enable syntax highlighting in the editor"), components).getComponent();

                final var enableStartTlS = new DarkComponent<>(new JCheckBox("Enable StartTLS"), components).getComponent();

                final var enableUTF8 = new DarkComponent<>(new JCheckBox("Enable UTF-8 by default"), components).getComponent();
            checkBoxes.add(editorInlined);
            checkBoxes.add(editorHighlighting);
            checkBoxes.add(enableStartTlS);
            checkBoxes.add(enableUTF8);

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
        panel.add(appearancePanel);
        panel.add(checkBoxes);
        panel.add(themePanel);

        getContentPane().add(panel);
        pack();

        final var settings = Settings.getInstance();

        stepper.addChangeListener(__ -> settings.setFontSize((Integer) stepper.getValue()));

        appearanceBox.addItem(Constants.UI.APPEARANCE_AUTO);
        appearanceBox.addItem(Constants.UI.APPEARANCE_DARK);
        appearanceBox.addItem(Constants.UI.APPEARANCE_LIGHT);
        if (settings.getAutoDarkMode()) {
            appearanceBox.setSelectedItem(Constants.UI.APPEARANCE_AUTO);
        } else {
            appearanceBox.setSelectedItem(settings.getDarkMode() ? Constants.UI.APPEARANCE_DARK : Constants.UI.APPEARANCE_LIGHT);
        }
        appearanceBox.addItemListener(this::appearanceChanged);

        editorInlined.setSelected(settings.getEditorInlined());
        editorHighlighting.setSelected(settings.getSyntaxHighlighting());
        enableStartTlS.setSelected(settings.getStartTLS());
        enableUTF8.setSelected(settings.useUTF8());

        editorInlined.addItemListener(__ -> settings.setEditorInlined(editorInlined.isSelected()));
        editorHighlighting.addItemListener(__ -> settings.setSyntaxHighlighting(editorHighlighting.isSelected()));
        enableStartTlS.addItemListener(__ -> settings.setStartTLS(enableStartTlS.isSelected()));
        enableUTF8.addItemListener(__ -> settings.setUseUTF8(enableUTF8.isSelected()));

        themeButton.addActionListener(__ -> themeButtonClick());

        themeBox.addItem(Constants.Editor.DEFAULT_THEME);
        themeBox.addItem(Constants.Editor.CHOOSE_THEME);
        final var themePath = settings.getEditorThemePath();
        if (!themePath.isBlank()) {
            themeBox.addItem(themePath);
            themeBox.setSelectedItem(themePath);
        } else {
            themeBox.setSelectedItem(Constants.Editor.DEFAULT_THEME);
            themeButton.setVisible(false);
        }
        themeBox.addItemListener(this::themeChanged);
    }

    /**
     * Stores the new selected appearance. Triggers an appearance
     * update if necessary.
     *
     * @param event the event
     */
    private void appearanceChanged(final ItemEvent event) {
        final var settings = Settings.getInstance();
        switch ((String) event.getItem()) {
            case Constants.UI.APPEARANCE_AUTO -> settings.setAutoDarkMode(true);

            case Constants.UI.APPEARANCE_DARK -> {
                settings.setAutoDarkMode(false);
                settings.setDarkMode(true);
            }

            case Constants.UI.APPEARANCE_LIGHT -> {
                settings.setAutoDarkMode(false);
                settings.setDarkMode(false);
            }
        }
    }

    /**
     * Adds functionality to the choosing process of the combo box.
     *
     * @param event the event
     * @see #themeBox
     */
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

    /**
     * Tries to open the given file as a theme file. Shows
     * an error message if the parsing failed.
     *
     * @param file the file to be opened
     * @return whether the file was opened and parsed successfully
     */
    private boolean tryOpen(final File file) {
        lastRead = null;
        try (final var is = new BufferedInputStream(new FileInputStream(file))) {
            final var theme = new JSONTheme();
            new JSONParser(new StringStream(new String(is.readAllBytes(), StandardCharsets.UTF_8))).readInto(theme);
            lastRead = theme;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read file:\n" + e.getLocalizedMessage(),
                    Constants.NAME + ": File error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Opens a file chooser for selecting a theme file.
     * Tries to open it and shows an error message if it
     * could not.
     *
     * @see #tryOpen(File)
     */
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
            if (Objects.equals(selected, Constants.Editor.DEFAULT_THEME)) {
                settings.setEditorThemePath(null);
            } else {
                settings.setEditorTheme((String) selected, lastRead);
            }
        }

        super.dispose();
    }
}
