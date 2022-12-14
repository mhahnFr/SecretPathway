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
public class EditorView extends JPanel implements DarkModeListener {
    /** A list consisting of all components enabling the dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The document responsible for highlighting the source code.  */
    private final SyntaxDocument document;

    /**
     * Initializes this EditorView.
     */
    public EditorView() {
        super(new BorderLayout());
        components.add(new DarkComponent<>(this));
            document = new SyntaxDocument();
            final var textPane = new DarkTextComponent<>(new JTextPane(document), components).getComponent();
            textPane.setFont(Constants.UI.FONT.deriveFont((float) Settings.getInstance().getFontSize()));
            final var scrollPane = new DarkComponent<>(new JScrollPane(textPane), components).getComponent();

            final var south = new DarkComponent<>(new JPanel(new GridLayout(3, 1)), components).getComponent();
                final var buttons = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                    final var highlight = new DarkComponent<>(new JCheckBox("Syntax highlighting"), components).getComponent();
                    highlight.addItemListener(__ -> toggleSyntaxHighlighting(highlight.isSelected()));
                    highlight.setSelected(Settings.getInstance().getSyntaxHighlighting());

                    final var closeButton = new JButton("Close");
                    closeButton.addActionListener(__ -> dispose());

                    final var saveButton = new JButton("Save");
                    saveButton.addActionListener(__ -> saveText());
                buttons.add(highlight, BorderLayout.CENTER);
                buttons.add(saveButton, BorderLayout.EAST);

                final var status = new DarkComponent<>(new JLabel("Status"), components).getComponent();
            south.add(buttons);
            south.add(status);
        add(scrollPane, BorderLayout.CENTER);
        add(south,      BorderLayout.SOUTH);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        Settings.getInstance().addDarkModeListener(this);
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

    public void dispose() {
        Settings.getInstance().removeDarkModeListener(this);
    }
}
