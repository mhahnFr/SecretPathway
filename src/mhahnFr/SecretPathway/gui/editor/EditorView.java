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

package mhahnFr.SecretPathway.gui.editor;

import mhahnFr.SecretPathway.core.Constants;
import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.utils.SettingsListener;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;
import mhahnFr.utils.gui.DarkTextComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a functional LPC source code editor view.
 *
 * @author mhahnFr
 * @since 05.01.23
 */
public class EditorView extends JPanel implements DarkModeListener, SettingsListener {
    /** A list consisting of all components enabling the dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The document responsible for highlighting the source code.  */
    private final SyntaxDocument document;
    /** The text pane.                                              */
    private final JTextPane textPane;
    /** The optional {@link DisposeListener}.                       */
    private DisposeListener disposeListener;

    /**
     * Initializes this EditorView.
     */
    public EditorView() {
        super(new BorderLayout());
        components.add(new DarkComponent<>(this));
            document = new SyntaxDocument();
            textPane = new DarkTextComponent<>(new JTextPane(document), components).getComponent();
            final var scrollPane = new DarkComponent<>(new JScrollPane(textPane), components).getComponent();

            final var south = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                final var buttons = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                    final var highlight = new DarkComponent<>(new JCheckBox("Syntax highlighting"), components).getComponent();
                    highlight.addItemListener(__ -> toggleSyntaxHighlighting(highlight.isSelected()));
                    highlight.setSelected(Settings.getInstance().getSyntaxHighlighting());

                    final var pushButtons = new DarkComponent<>(new JPanel(new GridLayout(1, 2)), components).getComponent();
                        final var closeButton = new JButton("Close");
                        closeButton.addActionListener(__ -> dispose());

                        final var saveButton = new JButton("Save");
                        saveButton.addActionListener(__ -> saveText());
                    pushButtons.add(closeButton);
                    pushButtons.add(saveButton);
                buttons.add(highlight,   BorderLayout.CENTER);
                buttons.add(pushButtons, BorderLayout.EAST);

                final var status = new DarkComponent<>(new JLabel("Status"), components).getComponent();
            south.add(buttons);
            south.add(status);
        add(scrollPane, BorderLayout.CENTER);
        add(south,      BorderLayout.SOUTH);
        setBorder(new EmptyBorder(5, 5, 5, 5));


        textPane.addCaretListener(e -> status.setText(document.getMessageFor(e.getDot())));

        final var settings = Settings.getInstance();
        settings.addDarkModeListener(this);
        settings.addListener(this);
        setDark(settings.getDarkMode());
        setFontSize(settings.getFontSize());
    }

    @Override
    public void darkModeToggled(boolean dark) {
        setDark(dark);
    }

    /**
     * Sets whether this component should appear in the dark mode.
     *
     * @param dark whether to use the dark mode
     */
    public void setDark(final boolean dark) {
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    /**
     * The listening function for settings changes.
     *
     * @param key the key of the changed setting
     * @param newValue the new value of the changed setting
     */
    public void settingChanged(final String key, final Object newValue) {
        if (key.equals(Settings.Keys.FONT_SIZE)) {
            setFontSize((Float) newValue);
        }
    }

    /**
     * Sets the font size of the main text pane.
     *
     * @param size the new size of the font
     * @see #textPane
     */
    private void setFontSize(final float size) {
        textPane.setFont(Constants.UI.FONT.deriveFont(size));
    }

    /**
     * Saves the text of the editor by sending a message to the server.
     */
    private void saveText() {
        // TODO: Save the text
        System.out.println("Saving...");
    }

    /**
     * Toggles the syntax highlighting. If it is disabled, the text styling is reset.
     *
     * @param enabled whether to apply syntax highlighting
     */
    private void toggleSyntaxHighlighting(final boolean enabled) {
        document.setHighlighting(enabled);
    }

    /**
     * Registers the given {@link DisposeListener}.
     *
     * @param disposeListener the listener to be registered
     */
    public void onDispose(final DisposeListener disposeListener) {
        this.disposeListener = disposeListener;
    }

    /**
     * Destroys this EditorView.
     */
    public void dispose() {
        final var settings = Settings.getInstance();

        settings.removeDarkModeListener(this);
        settings.removeListener(this);

        if (disposeListener != null) {
            disposeListener.onDispose(this);
        }
    }

    /**
     * This interface defines a dispose listener.
     *
     * @author mhahnFr
     * @since 03.02.23
     */
    public interface DisposeListener {
        /**
         * Called when the {@link EditorView} is disposed.
         *
         * @param view the actual {@link EditorView} that is disposed
         */
        void onDispose(final EditorView view);
    }
}
