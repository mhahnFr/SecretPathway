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

import mhahnFr.SecretPathway.core.lpc.interpreter.Definition;

import javax.swing.*;
import java.awt.*;

public class SuggestionLabel extends JPanel {
    private final Definition represented;
    private boolean selected;

    public SuggestionLabel(final Definition suggestion) {
        super(new BorderLayout());
            final var suggestionLabel = new JLabel(suggestion.getName());
            suggestionLabel.setOpaque(false);

            final var typeLabel = new JLabel(suggestion.getType().toString());
            typeLabel.setForeground(Color.gray);
            typeLabel.setOpaque(false);
        add(suggestionLabel, BorderLayout.WEST);
        add(typeLabel, BorderLayout.EAST);

        this.represented = suggestion;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
        if (selected) {
            setBackground(Color.blue);
        } else {
            setBackground(null);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public Definition getRepresented() {
        return represented;
    }
}
