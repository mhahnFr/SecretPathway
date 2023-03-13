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
import mhahnFr.SecretPathway.core.lpc.interpreter.FunctionDefinition;
import mhahnFr.utils.SettingsListener;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkTextComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a functional LPC source code editor view.
 *
 * @author mhahnFr
 * @since 05.01.23
 */
public class EditorView extends JPanel implements SettingsListener, FocusListener {
    /** A list consisting of all components enabling the dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The window displaying the available suggestions.            */
    private final SuggestionsWindow suggestionsWindow = new SuggestionsWindow();
    /** The document responsible for highlighting the source code.  */
    private final SyntaxDocument document = new SyntaxDocument();
    /** The text pane.                                              */
    private final JTextPane textPane;
    /** The status label.                                           */
    private final JLabel statusLabel;
    /** The button used for saving the document.                    */
    private final JButton saveButton;
    /** The button used for closing the editor.                     */
    private final JButton closeButton;
    /** The optional {@link DisposeListener}.                       */
    private DisposeListener disposeListener;

    /**
     * Initializes this EditorView.
     */
    public EditorView() {
        super(new BorderLayout());
        components.add(new DarkComponent<>(this));
            textPane = new DarkTextComponent<>(new JTextPane(document), components).getComponent();
            final var scrollPane = new DarkComponent<>(new JScrollPane(textPane), components).getComponent();

            final var south = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                statusLabel = new DarkComponent<>(new JLabel(), components).getComponent();

                final var buttons = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                    final var highlight = new DarkComponent<>(new JCheckBox("Syntax highlighting"), components).getComponent();
                    highlight.addItemListener(__ -> toggleSyntaxHighlighting(highlight.isSelected()));
                    highlight.setSelected(Settings.getInstance().getSyntaxHighlighting());

                    final var pushButtons = new DarkComponent<>(new JPanel(new GridLayout(1, 2)), components).getComponent();
                        closeButton = new JButton("Close");
                        closeButton.addActionListener(__ -> dispose());

                        final var compileButton = new JButton("Compile");
                        compileButton.addActionListener(__ -> compile());

                        saveButton = new JButton("Save");
                        saveButton.addActionListener(__ -> saveText());
                    pushButtons.add(closeButton);
                    pushButtons.add(compileButton);
                    pushButtons.add(saveButton);
                buttons.add(highlight,   BorderLayout.CENTER);
                buttons.add(pushButtons, BorderLayout.EAST);
            south.add(statusLabel);
            south.add(buttons);
        add(scrollPane, BorderLayout.CENTER);
        add(south,      BorderLayout.SOUTH);
        setBorder(new EmptyBorder(5, 5, 5, 5));


        textPane.addCaretListener(e -> statusLabel.setText(document.getMessageFor(e.getDot())));
        textPane.addFocusListener(this);

        document.setUpdateCallback(this::update);
        document.setCaretMover(delta -> textPane.setCaretPosition(textPane.getCaretPosition() + delta));

        final var settings = Settings.getInstance();
        settings.addListener(this);
        setDark(settings.getDarkMode());
        setFontSize(settings.getFontSize());
        addKeyActions();
    }

    @Override
    public void focusGained(FocusEvent e) {
        addKeyActions();
    }

    @Override
    public void focusLost(FocusEvent e) {
        removeKeyActions();
        if (suggestionsWindow.isVisible()) {
            toggleSuggestionMenu();
        }
    }

    /**
     * Updates document related UI parts. Asserts that the update
     * is executed in the {@link EventQueue} dispatch thread.
     */
    private void update() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this::update);
        } else {
            statusLabel.setText(document.getMessageFor(textPane.getCaretPosition()));
        }
    }

    /**
     * Adds the key actions that collide with the suggestion key actions
     * to the keymap.
     */
    private void addSaveCloseActions() {
        final var map = textPane.getKeymap();

        map.addActionForKeyStroke(Constants.Editor.SAVE_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveButton.doClick();
            }
        });
        map.addActionForKeyStroke(Constants.Editor.CLOSE_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeButton.doClick();
            }
        });
    }

    /**
     * Adds all keyboard actions associated with this editor
     * to the keymap of the actual text pane.
     *
     * @see #textPane
     */
    private void addKeyActions() {
        final var map = textPane.getKeymap();

        map.addActionForKeyStroke(Constants.Editor.SHOW_SUGGESTIONS, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSuggestionMenu();
            }
        });
        addSaveCloseActions();
    }

    /**
     * Removes the keyboard actions associated with this editor
     * from the keymap of the actual key map.
     *
     * @see #textPane
     */
    private void removeKeyActions() {
        final var map = textPane.getKeymap();

        map.removeKeyStrokeBinding(Constants.Editor.SHOW_SUGGESTIONS);
    }

    /**
     * Adds the keyboard actions needed when displaying the
     * suggestions window.
     */
    private void addSuggestionKeyActions() {
        final var m = textPane.getKeymap();
        m.removeKeyStrokeBinding(Constants.Editor.SAVE_KEY);
        m.removeKeyStrokeBinding(Constants.Editor.CLOSE_KEY);
        m.addActionForKeyStroke(Constants.Editor.POPUP_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                suggestionsWindow.selectNext();
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                suggestionsWindow.selectPrevious();
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_CLOSE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSuggestionMenu();
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_LEFT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSuggestionMenu();

                final var caretPosition = textPane.getCaretPosition();
                if (caretPosition > 0) {
                    textPane.setCaretPosition(caretPosition - 1);
                }
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_RIGHT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSuggestionMenu();

                final var caretPosition = textPane.getCaretPosition();
                if (caretPosition < document.getLength()) {
                    textPane.setCaretPosition(caretPosition + 1);
                }
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_ENTER, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    final var suggestion = suggestionsWindow.getSelected();
                    if (suggestion != null) {
                        document.insertString(textPane.getCaretPosition(), suggestion.getSuggestion(), null);
                        if (suggestion instanceof final DefinitionSuggestion definitionSuggestion &&
                            definitionSuggestion.definition() instanceof FunctionDefinition) {
                            // TODO: argument stumps
                            document.insertString(textPane.getCaretPosition(), "()", null);
                            textPane.setCaretPosition(textPane.getCaretPosition() - 1);
                        }
                    }
                } catch (BadLocationException exception) {
                    exception.printStackTrace();
                }
                toggleSuggestionMenu();
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_REPLACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Replace selection
            }
        });
    }

    /**
     * Removes the keyboard actions needed when displaying
     * the suggestions window.
     */
    private void removeSuggestionKeyActions() {
        final var m = textPane.getKeymap();
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_CLOSE);
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_UP);
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_DOWN);
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_LEFT);
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_RIGHT);
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_ENTER);
        m.removeKeyStrokeBinding(Constants.Editor.POPUP_REPLACE);
        addSaveCloseActions();
    }

    /**
     * Handles the main keyboard action of the suggestions window.
     */
    private void toggleSuggestionMenu() {
        if (suggestionsWindow.isVisible()) {
            suggestionsWindow.setVisible(false);
            removeSuggestionKeyActions();
        } else {
            suggestionsWindow.updateSuggestions(document.getAvailableSuggestions(textPane.getCaretPosition()));
            addSuggestionKeyActions();
            final Rectangle2D caretPosition;
            try {
                caretPosition = textPane.modelToView2D(textPane.getCaretPosition());
            } catch (BadLocationException e) {
                System.err.println("Impossible error:");
                e.printStackTrace();
                System.err.println("-----------------");
                return;
            }
            final var panePosition  = textPane.getLocationOnScreen();
            suggestionsWindow.setLocation((int) (caretPosition.getX() + panePosition.x),
                                          (int) (caretPosition.getY() + panePosition.y + Settings.getInstance().getFontSize()));
            suggestionsWindow.pack();
            suggestionsWindow.setVisible(true);
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return textPane.requestFocusInWindow();
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
    @Override
    public void settingChanged(final String key, final Object newValue) {
        switch (key) {
            case Settings.Keys.DARK_MODE         -> setDark((Boolean) newValue);
            case Settings.Keys.FONT_SIZE         -> setFontSize((Float) newValue);
            case Settings.Keys.EDITOR_THEME_PATH -> document.setTheme(Settings.getInstance().getEditorTheme());
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
     * Saves the text of the editor and compiles it by sending
     * a message to the server.
     */
    private void compile() {
        // TODO: Compile the text
        System.out.println("Compiling...");
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
        suggestionsWindow.dispose();
        removeKeyActions();
        final var settings = Settings.getInstance();

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
