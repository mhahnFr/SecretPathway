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
import mhahnFr.utils.gui.DarkComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Vector;

/**
 * This class represents a label with a displayed {@link Suggestion}.
 *
 * @author mhahnFr
 * @since 03.03.23
 */
public class SuggestionLabel extends JPanel {
    /** The represented and thus displayed suggestion.       */
    private final Suggestion represented;
    /** The label with the actual suggestion.                */
    private final JLabel suggestionLabel;
    /** A list enabling registered component's dark mode.    */
    private final java.util.List<DarkComponent<? extends JComponent>> components = new Vector<>(2);
    /** Indicates whether the dark mode is currently active. */
    private boolean dark;
    /** Indicates whether this label is selected.            */
    private boolean selected;

    /**
     * Constructs this label. The given {@link Suggestion} is represented
     * and displayed.
     *
     * @param suggestion the suggestion to be displayed
     * @param dark whether the dark mode is active
     */
    public SuggestionLabel(final Suggestion suggestion, final boolean dark) {
        super(new BorderLayout());
        components.add(new DarkComponent<>(this));
            suggestionLabel = new DarkComponent<>(new JLabel(suggestion.content() + (suggestion.isFunction() ? "()" : "")), components).getComponent();
            suggestionLabel.setFont(Constants.UI.FONT);
            suggestionLabel.setOpaque(false);
            suggestionLabel.setBorder(new EmptyBorder(0, 5, 0, 5));

            final var type = suggestion.type();
            final var typeLabel = new JLabel(type == null ? "<< unknown >>" : type.toString());
            typeLabel.setFont(Constants.UI.FONT);
            typeLabel.setForeground(type == null ? Color.red : Color.gray);
            typeLabel.setOpaque(false);
        add(suggestionLabel, BorderLayout.WEST);
        add(typeLabel, BorderLayout.EAST);

        this.represented = suggestion;
        setDark(dark);
    }

    /**
     * Toggles the dark mode of this component.
     *
     * @param dark whether to enable the dark mode
     */
    public void setDark(final boolean dark) {
        this.dark = dark;
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    /**
     * Sets whether this label should be rendered as being selected.
     *
     * @param selected whether this label is selected
     */
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

    /**
     * Returns whether this label is rendered as being selected.
     *
     * @return whether this label is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns the represented {@link Suggestion}.
     *
     * @return the represented suggestion
     */
    public Suggestion getRepresented() {
        return represented;
    }
}
