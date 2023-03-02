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

import mhahnFr.SecretPathway.core.Settings;
import mhahnFr.SecretPathway.core.lpc.interpreter.Definition;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;

import javax.swing.*;
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
    /** A list with all components enabling the dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The list with the suggestions to be displayed.     */
    private final List<Definition> suggestions = new Vector<>();
    private final JPanel suggestionPanel;

    /**
     * Constructs this window.
     */
    public SuggestionsWindow() {
        suggestionPanel = new DarkComponent<>(new JPanel(new GridLayout(1, 0)), components).getComponent();
        final var scrollPane = new DarkComponent<>(new JScrollPane(suggestionPanel), components).getComponent();

        getContentPane().add(scrollPane);

        Settings.getInstance().addDarkModeListener(this);
        darkModeToggled(Settings.getInstance().getDarkMode());
        setPreferredSize(new Dimension(300, 200));
        setFocusable(false);
    }

    @Override
    public void darkModeToggled(boolean dark) {
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    /**
     * Adds the given suggestion.
     *
     * @param suggestion the suggestion to be added
     */
    public void addSuggestion(final Definition suggestion) {
        suggestions.add(suggestion);
        addSuggestionGUI(suggestion);
    }

    private void addSuggestionGUI(final Definition suggestion) {
        suggestionPanel.add(new JLabel(suggestion.getName()));
    }

    /**
     * Adds the given suggestions.
     *
     * @param suggestions the suggestions to be added
     */
    public void addSuggestions(final Collection<Definition> suggestions) {
        this.suggestions.addAll(suggestions);
        for (final var suggestion : suggestions) {
            addSuggestionGUI(suggestion);
        }
    }

    /**
     * Updates the suggestions. Only suggestions in the given list
     * will be displayed.
     *
     * @param newSuggestions the new suggestions to be displayed
     */
    public void updateSuggestions(final Collection<Definition> newSuggestions) {
        suggestions.clear();
        suggestions.addAll(newSuggestions);
    }

    /**
     * Removes the given suggestion.
     *
     * @param suggestion the suggestion to be removed
     */
    public void removeSuggestion(final Definition suggestion) {
        suggestions.remove(suggestion);
        // TODO: Remove the suggestion
    }

    /**
     * Removes all suggestions.
     */
    public void clearSuggestions() {
        suggestions.clear();
        suggestionPanel.removeAll();
    }

    @Override
    public void dispose() {
        Settings.getInstance().removeDarkModeListener(this);
        super.dispose();
    }
}
