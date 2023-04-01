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

package mhahnFr.SecretPathway.gui.editor.suggestions;

import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * This class represents a window popup for displaying
 * code suggestions.
 *
 * @author mhahnFr
 * @since 01.03.23
 */
public class SuggestionsWindow extends JWindow implements DarkModeListener {
    /** A list with all components enabling the dark mode.      */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The list with the suggestions to be displayed.          */
    private final List<SuggestionLabel> suggestions = new Vector<>();
    /** The panel with the suggestion part panels.              */
    private final JPanel suggestionPanel;
    /** The panel with the left suggestion parts.               */
    private final JPanel suggestionLeftPanel;
    /** The panel with the right suggestion parts.              */
    private final JPanel suggestionRightPanel;
    /** The label indicating that no suggestions are available. */
    private final JLabel noSuggestionsLabel;
    /** The index of the currently selected suggestion.         */
    private int index;
    /** Whether the dark mode is enabled.                       */
    private boolean dark;

    /**
     * Constructs this window.
     */
    public SuggestionsWindow() {
        final var wrapperPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            suggestionPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            final var scrollPane = new DarkComponent<>(new JScrollPane(suggestionPanel), components).getComponent();
                suggestionLeftPanel = new DarkComponent<>(new JPanel(new GridLayout(0, 1)), components).getComponent();

                suggestionRightPanel = new DarkComponent<>(new JPanel(new GridLayout(0, 1)), components).getComponent();
            suggestionPanel.add(suggestionLeftPanel,  BorderLayout.CENTER);
            suggestionPanel.add(suggestionRightPanel, BorderLayout.EAST);

            final var insertionLabel = new JLabel("Insert using <ENTER> or replace using <TAB>");
            insertionLabel.setOpaque(false);
            insertionLabel.setForeground(Color.gray);
        wrapperPanel.add(scrollPane,   BorderLayout.CENTER);
        wrapperPanel.add(insertionLabel, BorderLayout.SOUTH);

        noSuggestionsLabel = new DarkComponent<>(new JLabel("No suggestions available"), components).getComponent();
        noSuggestionsLabel.setBorder(new EmptyBorder(0, 5, 0, 5));

        getContentPane().add(wrapperPanel);

        Settings.getInstance().addDarkModeListener(this);
        darkModeToggled(Settings.getInstance().getDarkMode());
        setFocusable(false);
    }

    @Override
    public void darkModeToggled(boolean dark) {
        this.dark = dark;
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    /**
     * Selects the next available suggestion. If the
     * currently selected suggestion is the last one,
     * the first suggestion is selected.
     *
     * @see #selectPrevious()
     * @see #getSelected()
     */
    public void selectNext() {
        if (!suggestions.isEmpty()) {
            final int newIndex;
            if (index + 1 >= suggestions.size()) {
                newIndex = 0;
            } else {
                newIndex = index + 1;
            }
            select(newIndex);
        }
    }

    /**
     * Selects the previous suggestion. If the currently
     * selected suggestion is the first one, the last one
     * is selected.
     *
     * @see #selectNext()
     * @see #getSelected()
     */
    public void selectPrevious() {
        if (!suggestions.isEmpty()) {
            final int newIndex;
            if (index - 1 < 0) {
                newIndex = suggestions.size() - 1;
            } else {
                newIndex = index - 1;
            }
            select(newIndex);
        }
    }

    /**
     * Selects the suggestion at the given index. Deselects
     * the currently selected one. The given index will be set
     * as the current index.
     *
     * @param newIndex the new index
     * @see #index
     */
    private void select(final int newIndex) {
        suggestions.get(index).setSelected(false);
        index = newIndex;

        final var suggestion = suggestions.get(index);
        final var height     = suggestion.getHeight();

        suggestion.setSelected(true);
        suggestionPanel.scrollRectToVisible(new Rectangle(suggestion.getX(),
                                                          suggestion.getY() - height * 2,
                                                          suggestion.getWidth(),
                                                          height * 5));
    }

    /**
     * Returns the currently selected suggestion.
     *
     * @return the currently selected suggestion
     * @see #selectNext()
     * @see #selectPrevious()
     */
    public Suggestion getSelected() {
        return suggestions.isEmpty() ? null : suggestions.get(index).getRepresented();
    }

    /**
     * Adds the given suggestion.
     *
     * @param suggestion the suggestion to be added
     */
    public void addSuggestion(final Suggestion suggestion) {
        final var label = new SuggestionLabel(suggestion, dark);
        suggestionLeftPanel.add(label.getLeftPart());
        suggestionRightPanel.add(label.getRightPart());
        suggestions.add(label);
    }

    /**
     * Adds the given suggestions.
     *
     * @param suggestions the suggestions to be added
     */
    public void addSuggestions(final Collection<Suggestion> suggestions) {
        for (final var suggestion : suggestions) {
            addSuggestion(suggestion);
        }
    }

    /**
     * Updates the suggestions. Only suggestions in the given list
     * will be displayed.
     *
     * @param newSuggestions the new suggestions to be displayed
     */
    public void updateSuggestions(final Collection<Suggestion> newSuggestions) {
        final Suggestion selected;
        if (suggestions.isEmpty()) {
            selected = null;
        } else {
            selected = suggestions.get(index).getRepresented();
        }
        clearSuggestions();
        index = 0;
        var found = false;
        for (final var suggestion : newSuggestions) {
            addSuggestion(suggestion);
            if (!suggestion.equals(selected) && !found) {
                ++index;
            } else {
                found = true;
            }
        }
        if (suggestions.isEmpty()) {
            suggestionLeftPanel.add(noSuggestionsLabel);
        } else {
            if (selected == null) {
                suggestionLeftPanel.remove(noSuggestionsLabel);
            }
            if (index >= suggestions.size()) {
                index = 0;
            }
            select(index);
        }
        validate();
        updateSize(true);
    }

    /**
     * Updates the size of this popup.
     *
     * @param pack indicates whether to {@link #pack()}
     */
    private void updateSize(final boolean pack) {
        if (suggestions.size() > 10) {
            final var suggestion = suggestions.get(0);
            setSize(suggestion.getWidth() + 20, suggestion.getHeight() * 10);
        } else if (pack) {
            pack();
        }
    }

    /**
     * Removes the given suggestion.
     *
     * @param suggestion the suggestion to be removed
     */
    public void removeSuggestion(final Suggestion suggestion) {
        SuggestionLabel toRemove = null;
        for (final var label : suggestions) {
            if (label.getRepresented().equals(suggestion)) {
                toRemove = label;
                break;
            }
        }
        if (toRemove != null) {
            if (toRemove.isSelected()) {
                selectPrevious();
            }
            suggestions.remove(toRemove);
            suggestionLeftPanel.remove(toRemove.getLeftPart());
            suggestionRightPanel.remove(toRemove.getRightPart());
        }
    }

    /**
     * Removes all suggestions.
     */
    public void clearSuggestions() {
        suggestions.clear();
        suggestionLeftPanel.removeAll();
        suggestionRightPanel.removeAll();
        index = 0;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            if (suggestions.isEmpty()) {
                suggestionLeftPanel.add(noSuggestionsLabel);
            } else {
                select(0);
            }
        } else {
            if (suggestions.isEmpty()) {
                suggestionLeftPanel.remove(noSuggestionsLabel);
            }/* else {
                suggestions.get(index).setSelected(false);
            }*/
        }
        updateSize(false);
        super.setVisible(b);
    }

    @Override
    public void dispose() {
        Settings.getInstance().removeDarkModeListener(this);
        super.dispose();
    }
}
