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
import mhahnFr.SecretPathway.core.lpc.LPCFileManager;
import mhahnFr.SecretPathway.core.lpc.interpreter.FunctionDefinition;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTTypeDefinition;
import mhahnFr.SecretPathway.gui.editor.suggestions.DefinitionSuggestion;
import mhahnFr.SecretPathway.gui.editor.suggestions.SuggestionType;
import mhahnFr.SecretPathway.gui.editor.suggestions.SuggestionsWindow;
import mhahnFr.utils.SettingsListener;
import mhahnFr.utils.gui.components.DarkComponent;
import mhahnFr.utils.gui.components.DarkTextComponent;
import mhahnFr.utils.gui.components.HintTextField;
import mhahnFr.utils.gui.components.SearchReplacePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a functional LPC source code editor view.
 *
 * @author mhahnFr
 * @since 05.01.23
 */
public class EditorView extends JPanel implements SettingsListener, FocusListener, SuggestionShower {
    /** A list consisting of all components enabling the dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The window displaying the available suggestions.            */
    private final SuggestionsWindow suggestionsWindow = new SuggestionsWindow();
    /** The document responsible for highlighting the source code.  */
    private final SyntaxDocument document;
    /** The text pane.                                              */
    private final JTextPane textPane;
    /** The status label.                                           */
    private final JLabel statusLabel;
    /** The button used for saving the document.                    */
    private final JButton saveButton;
    /** The button used for closing the editor.                     */
    private final JButton closeButton;
    private final SearchReplacePanel searchPanel;
    /** The LPC file manager.                                       */
    private final LPCFileManager loader;
    /** The name of the opened file.                                */
    private String name;
    /** The contents of the file when it was saved the last time.   */
    private String lastContent;
    /** Indicates whether to create only super suggestions.         */
    private boolean onlySupers;
    /** The optional {@link DisposeListener}.                       */
    private DisposeListener disposeListener;

    /**
     * Initializes this EditorView.
     *
     * @param loader the loader for loading referenced LPC source files
     */
    public EditorView(final LPCFileManager loader) {
        this(loader, null);
    }

    /**
     * Initializes this editor view. If a file name is given,
     * the given loader is used to load the contents of the file.
     *
     * @param loader the file manager
     * @param name   the name of the opened file
     */
    public EditorView(final LPCFileManager loader,
                      final String         name) {
        super(new BorderLayout());

        this.loader = loader;
        this.name   = name;
        document    = new SyntaxDocument(this.loader);

        components.add(new DarkComponent<>(this));
            searchPanel = new SearchReplacePanel();

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

                        final JButton compileButton;
                        if (loader.canCompile()) {
                            compileButton = new JButton("Compile");
                            compileButton.addActionListener(__ -> compile());
                        } else {
                            compileButton = null;
                        }

                        saveButton = new JButton("Save");
                        saveButton.addActionListener(__ -> saveText());
                    pushButtons.add(closeButton);
                    if (compileButton != null) {
                        pushButtons.add(compileButton);
                    }
                    pushButtons.add(saveButton);
                buttons.add(highlight,   BorderLayout.CENTER);
                buttons.add(pushButtons, BorderLayout.EAST);
            south.add(statusLabel);
            south.add(buttons);
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(south,       BorderLayout.SOUTH);
        setBorder(new EmptyBorder(5, 5, 5, 5));


        searchPanel.install(textPane);
        searchPanel.setVisible(false);
        textPane.addCaretListener(e -> statusLabel.setText(document.getMessageFor(e.getDot())));
        textPane.addFocusListener(this);
        addPopupMenu();

        document.setUpdateCallback(this::update);
        document.setCaretMover(delta -> textPane.setCaretPosition(textPane.getCaretPosition() + delta));
        document.setSuggestionShower(this);
        if (this.name != null) {
            loadFile();
        } else {
            lastContent = "";
        }

        final var settings = Settings.getInstance();
        settings.addListener(this);
        setDark(settings.getDarkMode());
        setFontSize(settings.getFontSize());
        addKeyActions();
    }

    /**
     * Creates and adds the popup menu to the text pane.
     */
    private void addPopupMenu() {
        final var menu = new JPopupMenu();
            final var suggestionsItem = new JMenuItem("Toggle suggestions");
            suggestionsItem.setAccelerator(Constants.Editor.SHOW_SUGGESTIONS);
            suggestionsItem.addActionListener(__ -> toggleSuggestionMenu());

            final var saveItem = new JMenuItem("Save");
            saveItem.setAccelerator(Constants.Editor.SAVE_KEY);
            saveItem.addActionListener(__ -> saveText());

            final var compileItem = new JMenuItem("Compile");
            compileItem.addActionListener(__ -> compile());
            compileItem.setEnabled(loader.canCompile());

            final var closeItem = new JMenuItem("Close");
            closeItem.setAccelerator(Constants.Editor.CLOSE_KEY);
            closeItem.addActionListener(__ -> dispose());

            final var undoItem = new JMenuItem("Undo");
            undoItem.setAccelerator(Constants.Editor.UNDO);
            undoItem.addActionListener(__ -> undoAction());

            final var redoItem = new JMenuItem("Redo");
            redoItem.setAccelerator(Constants.Editor.REDO);
            redoItem.addActionListener(__ -> redoAction());

            final var searchItem = new JMenuItem("Search");
            searchItem.setAccelerator(Constants.Editor.SEARCH);
            searchItem.addActionListener(null); // TODO

            final var replaceItem = new JMenuItem("Replace");
            replaceItem.setAccelerator(Constants.Editor.REPLACE);
            replaceItem.addActionListener(null); // TODO
        menu.add(suggestionsItem);
        menu.addSeparator();
        menu.add(saveItem);
        menu.add(compileItem);
        menu.addSeparator();
        menu.add(closeItem);
        menu.addSeparator();
        menu.add(undoItem);
        menu.add(redoItem);
        menu.addSeparator();
        menu.add(searchItem);
        menu.add(redoItem);

        textPane.setComponentPopupMenu(menu);
    }

    /**
     * Attempts to load and insert the file this view should represent.
     * Shows an error message if the file could not be loaded.
     */
    private void loadFile() {
        statusLabel.setText("Loading \"" + name + "\" ...");
        new Thread(() -> {
            String content = null;
            try {
                content = loader.load(name);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Could not load file '" + name + "'!",
                        Constants.NAME + ": Editor",
                        JOptionPane.ERROR_MESSAGE);
                name = null;
            }
            final var contentCopy = content;
            EventQueue.invokeLater(() -> {
                if (contentCopy == null) {
                    statusLabel.setText("Loading failed!");
                } else {
                    try {
                        document.insertString(0, contentCopy, null);
                        lastContent = contentCopy;
                    } catch (Exception e) {
                        System.err.println("Should not happen:");
                        e.printStackTrace();
                        System.err.println("------------------");
                    }
                    statusLabel.setText("Loading \"" + name + "\" done.");
                }
            });
        }).start();
    }

    @Override
    public void beginSuggestions() {
        if (!suggestionsWindow.isVisible()) {
            toggleSuggestionMenu();
        }
    }

    @Override
    public void beginSuperSuggestions() {
        onlySupers = true;
        beginSuggestions();
    }

    @Override
    public void endSuggestions() {
        if (suggestionsWindow.isVisible()) {
            toggleSuggestionMenu();
        }
    }

    @Override
    public void updateSuggestions() {
        updateSuggestionsImpl(null, null);
    }

    @Override
    public void updateSuggestionContext(final SuggestionType type, final ASTTypeDefinition expected) {
        updateSuggestionsImpl(type, expected);
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
            if (suggestionsWindow.isVisible()) {
                updateSuggestionsImpl(null, null);
            }
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
        map.addActionForKeyStroke(Constants.Editor.UNDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoAction();
            }
        });
        map.addActionForKeyStroke(Constants.Editor.REDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redoAction();
            }
        });
        map.addActionForKeyStroke(Constants.Editor.SEARCH, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPanel.setVisible(!searchPanel.isVisible());
                searchPanel.setReplace(false);
            }
        });
        map.addActionForKeyStroke(Constants.Editor.REPLACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchPanel.isVisible() && !searchPanel.isReplace()) {
                    searchPanel.setReplace(true);
                } else {
                    searchPanel.setVisible(!searchPanel.isVisible());
                    searchPanel.setReplace(true);
                }
            }
        });

        addSaveCloseActions();
    }

    /**
     * Redoes the last undid registered action if possible.
     */
    private void redoAction() {
        try {
            document.redo();
        } catch (BadLocationException ex) {
            System.err.println("Should not happen:");
            ex.printStackTrace();
            System.err.println("------------------");
        }
    }

    /**
     * Undoes the last registered action if possible.
     */
    private void undoAction() {
        try {
            document.undo();
        } catch (BadLocationException ex) {
            System.err.println("Should not happen:");
            ex.printStackTrace();
            System.err.println("------------------");
        }
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
        map.removeKeyStrokeBinding(Constants.Editor.UNDO);
        map.removeKeyStrokeBinding(Constants.Editor.REDO);
        map.removeKeyStrokeBinding(Constants.Editor.SEARCH);
        map.removeKeyStrokeBinding(Constants.Editor.REPLACE);
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
                insertSelectedSuggestion(false);
            }
        });
        m.addActionForKeyStroke(Constants.Editor.POPUP_REPLACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertSelectedSuggestion(true);
            }
        });
    }

    /**
     * Inserts the currently selected {@link mhahnFr.SecretPathway.gui.editor.suggestions.Suggestion}.
     *
     * @param replace whether to replace the current word by the suggestion
     */
    private void insertSelectedSuggestion(final boolean replace) {
        try {
            final var suggestion = suggestionsWindow.getSelected();
            if (suggestion != null) {
                document.insertSuggestion(textPane.getCaretPosition(), suggestion, replace);
                if (suggestion instanceof final DefinitionSuggestion definitionSuggestion &&
                        definitionSuggestion.getDefinition() instanceof final FunctionDefinition functionDefinition) {
                    // TODO: argument stumps
                    document.insertString(textPane.getCaretPosition(), "()", null);
                    if (!functionDefinition.getParameters().isEmpty()) {
                        textPane.setCaretPosition(textPane.getCaretPosition() - 1);
                    }
                }
            }
        } catch (BadLocationException exception) {
            exception.printStackTrace();
        }
        if (suggestionsWindow.isVisible()) {
            toggleSuggestionMenu();
        }
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
     * Updates the suggestions displayed by the {@link #suggestionsWindow}.
     *
     * @return the beginning position
     */
    private int updateSuggestionsImpl(final SuggestionType type, final ASTTypeDefinition expected) {
        final var caretPosition = textPane.getCaretPosition();

        final var suggestions = onlySupers ? document.getSuperSuggestions()
                                           : document.getAvailableSuggestions(caretPosition);

        if (type == null && expected == null) {
            document.computeSuggestionContext(caretPosition);
        }

        final var position = textPane.getCaretPosition();
        var toReturn       = position;
        try {
            if (expected != null) {
                suggestions.sort((a, b) -> {
                    final var aa = a.getRightSite();
                    final var bb = b.getRightSite();

                    if (Objects.equals(aa, expected.toString())) {
                        return -1;
                    } else if (Objects.equals(bb, expected.toString())) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
            }
            if (document.isInWord(position)) {
                final var begin = document.getWordBegin(position);
                toReturn = begin;
                final var wordBegin = document.getText(begin, position - begin);
                suggestions.removeIf(suggestion -> {
                    final var desc = suggestion.getDescription();

                    return suggestion.getSuggestion() == null || desc == null || !desc.contains(wordBegin);
                });
                suggestions.sort((a, b) -> {
                    final String aDesc = a.getDescription(),
                                 bDesc = b.getDescription();

                    boolean aa = aDesc != null && aDesc.startsWith(wordBegin),
                            bb = bDesc != null && bDesc.startsWith(wordBegin);

                    if (!aa && !bb) {
                        aa = aDesc != null && aDesc.contains(wordBegin);
                        bb = bDesc != null && bDesc.contains(wordBegin);
                    }
                    if (aa == bb) {
                        return 0;
                    } else if (aa) {
                        return -1;
                    } else {
                        return 1;
                    }
                });
            }
            suggestions.removeIf(s -> s.getSuggestion() == null);
        } catch (BadLocationException e) {
            System.err.println("Impossible error:");
            e.printStackTrace();
            System.err.println("-----------------");
        }
        suggestionsWindow.updateSuggestions(suggestions);
        return toReturn;
    }

    /**
     * Handles the main keyboard action of the suggestions window.
     */
    private void toggleSuggestionMenu() {
        if (suggestionsWindow.isVisible()) {
            suggestionsWindow.setVisible(false);
            removeSuggestionKeyActions();
            onlySupers = false;
        } else {
            final Rectangle2D caretPosition;
            try {
                caretPosition = textPane.modelToView2D(updateSuggestionsImpl(null, null));
            } catch (BadLocationException e) {
                System.err.println("Impossible error:");
                e.printStackTrace();
                System.err.println("-----------------");
                return;
            }
            addSuggestionKeyActions();
            final var panePosition = textPane.getLocationOnScreen();
            suggestionsWindow.setLocation((int) (caretPosition.getX() + panePosition.x),
                                          (int) (caretPosition.getY() + panePosition.y + Settings.getInstance().getFontSize() + 5));
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
        searchPanel.darkModeToggled(dark);
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
    private boolean saveText() {
        if (name == null) {
            final var result = JOptionPane.showInputDialog(this,
                                                           "Enter the name of the file:",
                                                           Constants.NAME + ": Editor",
                                                            JOptionPane.PLAIN_MESSAGE);
            if (result != null && !result.isBlank()) {
                name = result;
            } else return false;
        }
        final var content = document.getAllText();
        try {
            loader.save(name, content);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                                          "Could not save the file!",
                                          Constants.NAME + ": Editor",
                                          JOptionPane.ERROR_MESSAGE);
            return false;
        }
        lastContent = content;
        return true;
    }

    /**
     * Saves the text of the editor and compiles it by sending
     * a message to the server.
     */
    private void compile() {
        if (!loader.canCompile()) {
            JOptionPane.showMessageDialog(this,
                                          "Compiling is not supported!",
                                          Constants.NAME + ": Editor",
                                           JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (saveText()) {
            loader.compile(name);
        }
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
    public boolean dispose() {
        if (!Objects.equals(lastContent, document.getAllText())) {
            switch (JOptionPane.showConfirmDialog(this,
                                                  "There are unsaved changes.\nDo you want to save them?",
                                                  Constants.NAME + ": Editor",
                                                  JOptionPane.YES_NO_CANCEL_OPTION,
                                                  JOptionPane.WARNING_MESSAGE)) {
                case JOptionPane.YES_OPTION -> {
                    saveText();
                    return dispose();
                }

                case JOptionPane.NO_OPTION -> { /* Continue disposing. */ }

                default -> { return false; }
            }
        }

        suggestionsWindow.dispose();
        removeKeyActions();
        final var settings = Settings.getInstance();

        settings.removeListener(this);

        if (disposeListener != null) {
            disposeListener.onDispose(this);
        }
        return true;
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
