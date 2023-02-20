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
import mhahnFr.SecretPathway.core.lpc.parser.Parser;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTMissing;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTWrong;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Tokenizer;
import mhahnFr.SecretPathway.gui.editor.theme.DefaultTheme;
import mhahnFr.SecretPathway.gui.editor.theme.json.JSONTheme;
import mhahnFr.SecretPathway.gui.editor.theme.SPTheme;
import mhahnFr.utils.Pair;
import mhahnFr.utils.StringStream;

import javax.swing.text.*;
import java.util.HashMap;
import java.util.Map;

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
    /** The theme to be used for the syntax highlighting.     */
    private SPTheme theme = Settings.getInstance().getEditorTheme();//restoreTheme();
    private Map<Pair<Integer, Integer>, String> errorRanges = new HashMap<>();

    /**
     * Tries to restore the previously used theme. If it is not possible,
     * a new {@link DefaultTheme} is returned.
     *
     * @return the restored or a default theme
     */
    private SPTheme restoreTheme() {
        final var themePath = Settings.getInstance().getEditorThemePath();
        if (!themePath.isBlank()) {
            final var theme = JSONTheme.from(themePath);
            if (theme != null) { return theme; }
        }
        return new DefaultTheme();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offs, str, a);
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

        final var tokenizer = new Tokenizer(new StringStream(getAllText()));
        tokenizer.setCommentTokensEnabled(true);

        Token token;
        while ((token = tokenizer.nextToken()).type() != TokenType.EOF) {
            setCharacterAttributes(token.begin(), token.end() - token.begin(),
                    theme.styleFor(token.type()).asStyle(def), true);
        }

        errorRanges.clear();
        final var expressions = new Parser(getAllText()).parse();
        try {
            for (int i = 0; i < expressions.length; ++i) {
                System.out.println(expressions[i].describe(0));

                expressions[i].visit(expression -> {
                    final String errorText;
                    if (expression instanceof ASTMissing) {
                        errorText = ((ASTMissing) expression).getMessage();
                    } else if (expression instanceof ASTWrong) {
                        errorText = ((ASTWrong) expression).getMessage();
                    } else {
                        errorText = null;
                    }

                    switch (expression.getASTType()) {
                        case MISSING, WRONG -> {
                            errorRanges.put(new Pair<>(expression.getBegin().position(), expression.getEnd().position()), errorText);
                        }
                    }
                    final var style = theme.styleFor(expression.getASTType());
                    if (style != null) {
                        setCharacterAttributes(expression.getBegin().position(), expression.getEnd().position() - expression.getBegin().position(), style.asStyle(def), true);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println();
    }

    /**
     * Clears the syntax highlighting.
     */
    private void clearHighlight() {
        setCharacterAttributes(0, getLength(), def, true);
    }

    public String getMessageFor(int position) {
        for (final var entry : errorRanges.entrySet()) {
            if (position >= entry.getKey().getFirst() && position <= entry.getKey().getSecond()) {
                return entry.getValue();
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
}
