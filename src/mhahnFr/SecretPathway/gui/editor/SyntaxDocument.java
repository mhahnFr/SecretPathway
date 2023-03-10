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
import mhahnFr.SecretPathway.core.lpc.interpreter.Context;
import mhahnFr.SecretPathway.core.lpc.interpreter.Interpreter;
import mhahnFr.SecretPathway.core.lpc.parser.Parser;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Tokenizer;
import mhahnFr.SecretPathway.gui.editor.theme.SPTheme;
import mhahnFr.utils.StringStream;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class serves as a syntax aware document for LPC
 * source code.
 *
 * @author mhahnFr
 * @since 07.01.23
 */
public class SyntaxDocument extends DefaultStyledDocument {
    /** The default underlying style.                         */
    private final Style def = getLogicalStyle(0);
    /** Indicates whether the syntax highlighting is enabled. */
    private boolean highlighting;
    /** The callback to be called after parsing the content.  */
    private Runnable updateCallback;
    /** The theme to be used for the syntax highlighting.     */
    private SPTheme theme = Settings.getInstance().getEditorTheme();
    /** The interpreted context of the source code.           */
    private volatile Context context;
    /** The ranges containing syntax errors.                  */
    private volatile List<Highlight<?>> highlights;
    /** The execution service for interpreting the code.      */
    private final ExecutorService thread = Executors.newSingleThreadExecutor();

    private boolean isWhitespace(final int offset) {
        try {
            return getText(offset, 1).isBlank();
        } catch (BadLocationException __) {
            return true;
        }
    }

    private String getPreviousIndent(int offset) throws BadLocationException {
        int lineBegin;
        for (lineBegin = offset > 0 ? offset - 1 : 0; lineBegin > 0 && !getText(lineBegin, 1).equals("\n"); --lineBegin);
        lineBegin = lineBegin > 0 ? ++lineBegin : 0;
        int indent;
        for (indent = 0; lineBegin < offset && getText(lineBegin, 1).equals(" "); ++lineBegin, ++indent);
        return " ".repeat(indent);
    }

    @Override
    public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
        final String insertion;
        switch (str) {
            case "\t" -> insertion = "    ";

            // TODO: Cursor position
            case "(" -> insertion = isWhitespace(offs) ? "()" : str;
            case "{" -> insertion = isWhitespace(offs) ? "{}" : str;
            case "[" -> insertion = isWhitespace(offs) ? "[]" : str;

            case "\n" -> {
                insertion = str + getPreviousIndent(offs);
            }

            default -> insertion = str;
        }

        super.insertString(offs, insertion, a);
        maybeUpdateHighlight();
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        maybeUpdateHighlight();
    }

    /**
     * Returns the whole text of this document.
     *
     * @return the whole text
     */
    public String getAllText() {
        try {
            return getText(0, getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the currently used syntax highlighting theme.
     *
     * @return the syntax highlighting theme
     */
    public SPTheme getTheme() {
        return theme;
    }

    /**
     * Sets the syntax highlighting theme to be used. Does not
     * trigger a recoloring.
     *
     * @param theme the new theme to be used
     */
    public void setTheme(SPTheme theme) {
        this.theme = theme;
        maybeUpdateHighlight();
    }

    /**
     * Updates the syntax-highlighting if it is activated.
     *
     * @see #highlighting
     * @see #setHighlighting(boolean)
     * @see #isHighlighting()
     */
    private void maybeUpdateHighlight() {
        if (highlighting) {
            updateHighlight();
        }
    }

    /**
     * Updates the syntax highlight. If no theme is set,
     * nothing is done.
     *
     * @see #getTheme()
     * @see #setTheme(SPTheme)
     * @see #theme
     */
    private void updateHighlight() {
        if (theme == null) return;

        clearHighlight();

        final var text = getAllText();

        final var tokenizer = new Tokenizer(new StringStream(text));
        tokenizer.setCommentTokensEnabled(true);

        final var comments = new ArrayList<Token>();

        Token token;
        while ((token = tokenizer.nextToken()).type() != TokenType.EOF) {
            final var style = theme.styleFor(token.type());
            setCharacterAttributes(token.begin(), token.end() - token.begin(),
                    style == null ? def : style.asStyle(def), true);
            if (token.is(TokenType.COMMENT_LINE) || token.is(TokenType.COMMENT_BLOCK)) {
                comments.add(token);
            }
        }

        thread.execute(() -> {
            final var interpreter = new Interpreter();
            this.context    = interpreter.createContextFor(new Parser(text).parse());
            this.highlights = interpreter.getHighlights();
            for (final var range : highlights) {
                final var style = theme.styleFor(range.getType());
                if (style != null) {
                    setCharacterAttributes(range.getBegin(), range.getEnd() - range.getBegin(), style.asStyle(def), false);
                }
            }
            for (final var comment : comments) {
                final var style = theme.styleFor(comment.type());
                setCharacterAttributes(comment.begin(), comment.end() - comment.begin(),
                        style == null ? def : style.asStyle(def), true);
            }
            if (updateCallback != null) {
                updateCallback.run();
            }
        });
    }

    /**
     * Returns a list with the available suggestions at the
     * given text position.
     *
     * @param position the text position
     * @return a list with the suggestions
     */
    public List<Suggestion> getAvailableSuggestions(final int position) {
        if (context == null) {
            return null;
        }
        return context.availableDefinitions(position);
    }

    /**
     * Clears the syntax highlighting.
     */
    private void clearHighlight() {
        setCharacterAttributes(0, getLength(), def, true);
    }

    /**
     * Returns a message about the code at the given
     * cursor position.
     *
     * @param position the position of the cursor
     * @return the message at that position
     */
    public String getMessageFor(int position) {
        for (final var entry : highlights) {
            if (position >= entry.getBegin() && position <= entry.getEnd()
                    && entry instanceof MessagedHighlight<?>) {
                return ((MessagedHighlight<?>) entry).getMessage();
            }
        }
        return "";
    }

    /**
     * Returns whether the syntax highlighting is currently enabled.
     *
     * @return whether the syntax highlighting is enabled
     */
    public boolean isHighlighting() {
        return highlighting;
    }

    /**
     * Sets whether the syntax highlighting is enabled. If {@code false}
     * is passed, the highlight is reset.
     *
     * @param highlighting whether to highlight the syntax
     */
    public void setHighlighting(boolean highlighting) {
        this.highlighting = highlighting;

        if (highlighting) {
            updateHighlight();
        } else {
            clearHighlight();
        }
    }

    /**
     * Returns the update callback, which is called after the content
     * has been parsed.
     *
     * @return the update callback
     */
    public Runnable getUpdateCallback() {
        return updateCallback;
    }

    /**
     * Sets the update callback, which is called after the content
     * has been parsed. If a callback has been set already, it is
     * replaced.
     *
     * @param updateCallback the new update callback
     */
    public void setUpdateCallback(Runnable updateCallback) {
        this.updateCallback = updateCallback;
    }
}
