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
import mhahnFr.utils.SettingsListener;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.gui.components.DarkComponent;
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
public class SettingsWindow extends JDialog implements SettingsListener {
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

        final var settings = Settings.getInstance();
        settings.addListener(this);

        setDark(settings.getDarkMode());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    @Override
    public void settingChanged(final String key, final Object newValue) {
        switch (key) {
            case Settings.Keys.DARK_MODE -> setDark((Boolean) newValue);
            case Settings.Keys.NATIVE_LF -> SwingUtilities.updateComponentTreeUI(this);
        }
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

            final var checkBoxes = new DarkComponent<>(new JPanel(new GridLayout(6, 1)), components).getComponent();
                final var darkBox = new DarkComponent<>(new JCheckBox("Enable dark mode"), components).getComponent();

                final var nativeLF = new DarkComponent<>(new JCheckBox("Use native Look & Feel"), components).getComponent();

                final var editorInlined = new DarkComponent<>(new JCheckBox("Use inlined editor"), components).getComponent();

                final var editorHighlighting = new DarkComponent<>(new JCheckBox("Automatically enable syntax highlighting in the editor"), components).getComponent();

                final var enableStartTlS = new DarkComponent<>(new JCheckBox("Enable StartTLS"), components).getComponent();

                final var enableUTF8 = new DarkComponent<>(new JCheckBox("Enable UTF-8 by default"), components).getComponent();
            checkBoxes.add(darkBox);
            checkBoxes.add(nativeLF);
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
        panel.add(checkBoxes);
        panel.add(themePanel);

        getContentPane().add(panel);
        pack();

        final var settings = Settings.getInstance();

        stepper.addChangeListener(__ -> settings.setFontSize((Integer) stepper.getValue()));

        darkBox.setSelected(settings.getDarkMode());
        nativeLF.setSelected(settings.getNativeLookAndFeel());
        editorInlined.setSelected(settings.getEditorInlined());
        editorHighlighting.setSelected(settings.getSyntaxHighlighting());
        enableStartTlS.setSelected(settings.getStartTLS());
        enableUTF8.setSelected(settings.useUTF8());

        darkBox.addItemListener(__ -> settings.setDarkMode(darkBox.isSelected()));
        nativeLF.addItemListener(this::updateLookAndFeel);
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
     * Updates the Look & Feel.
     *
     * @param event the item event
     */
    private void updateLookAndFeel(final ItemEvent event) {
        final var settings  = Settings.getInstance();
        final var activated = event.getStateChange() == ItemEvent.SELECTED;

        if (settings.getNativeLookAndFeel() != activated) {
            try {
                UIManager.setLookAndFeel(activated ? UIManager.getSystemLookAndFeelClassName()
                                                   : UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                System.err.println("Could not set L&F:");
                e.printStackTrace();
                System.err.println("------------------");
                JOptionPane.showMessageDialog(this,
                                              "Could not change the Look&Feel!\n" +
                                              "Restart " + Constants.NAME + " to apply the change.",
                                              Constants.NAME + ": Settings",
                                              JOptionPane.ERROR_MESSAGE);
            }
            settings.setNativeLookAndFeel(activated);
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
    public void dispose() {
        settings.removeListener(this);

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
