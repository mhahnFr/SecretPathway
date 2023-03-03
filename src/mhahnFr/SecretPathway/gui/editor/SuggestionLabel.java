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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Vector;

public class SuggestionLabel extends JPanel {
    private final Suggestion represented;
    private final JLabel suggestionLabel;
    private final java.util.List<DarkComponent<? extends JComponent>> components = new Vector<>(2);
    private boolean dark;
    private boolean selected;

    public SuggestionLabel(final Suggestion suggestion, final boolean dark) {
        super(new BorderLayout());
        components.add(new DarkComponent<>(this));
            suggestionLabel = new DarkComponent<>(new JLabel(suggestion.content()), components).getComponent();
            suggestionLabel.setOpaque(false);
            suggestionLabel.setBorder(new EmptyBorder(0, 5, 0, 5));

            final var typeLabel = new JLabel(suggestion.type().toString());
            typeLabel.setForeground(Color.gray);
            typeLabel.setOpaque(false);
        add(suggestionLabel, BorderLayout.WEST);
        add(typeLabel, BorderLayout.EAST);

        this.represented = suggestion;
        setDark(dark);
    }

    public void setDark(final boolean dark) {
        this.dark = dark;
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
        if (selected) {
            setBackground(Color.blue);
            suggestionLabel.setForeground(Color.white);
        } else {
            setBackground(null);
            suggestionLabel.setForeground(dark ? Color.white : Color.black);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public Suggestion getRepresented() {
        return represented;
    }
}
