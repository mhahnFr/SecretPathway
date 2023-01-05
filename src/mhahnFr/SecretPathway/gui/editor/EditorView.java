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

import mhahnFr.utils.gui.DarkComponent;
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
public class EditorView extends JPanel {
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();

    public EditorView() {
        super(new BorderLayout());
            final var textPane = new DarkTextComponent<>(new JTextPane(), components).getComponent();
            final var scrollPane = new DarkComponent<>(new JScrollPane(textPane), components).getComponent();

            final var south = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                final var buttons = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                    final var highlight = new DarkComponent<>(new JCheckBox("Syntax highlighting"), components).getComponent();

                    final var saveButton = new JButton("Save");
                buttons.add(highlight, BorderLayout.CENTER);
                buttons.add(saveButton, BorderLayout.EAST);

                final var status = new DarkComponent<>(new JLabel("Status"), components).getComponent();
            south.add(buttons);
            south.add(status);
        add(scrollPane, BorderLayout.CENTER);
        add(south,      BorderLayout.SOUTH);
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    public void setDark(final boolean dark) {
        for (final var component : components) {
            component.setDark(dark);
        }
    }
}
