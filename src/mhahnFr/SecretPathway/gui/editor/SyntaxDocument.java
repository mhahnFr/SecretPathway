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

import mhahnFr.SecretPathway.core.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.parser.tokenizer.Tokenizer;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.gui.abstraction.FStyle;

import javax.swing.text.*;
import java.awt.Color;

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

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offs, str, a);
        updateHighlight();
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        updateHighlight();
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
     * Updates the syntax highlight.
     */
    private void updateHighlight() {
        final var tokenizer = new Tokenizer(new StringStream(getAllText()));
        tokenizer.setCommentTokensEnabled(true);

        final var style = new FStyle();

        Token token;
        while ((token = tokenizer.nextToken()).type() != TokenType.EOF) {
            switch (token.type()) {
                case IDENTIFIER -> style.setForeground(Color.green);

                case COMMENT_LINE, COMMENT_BLOCK -> {
                    style.setItalic(true);
                    style.setForeground(Color.gray);
                }

                case STRING -> style.setForeground(Color.red);

                case INCLUDE, INHERIT, PRIVATE, PROTECTED, PUBLIC, OVERRIDE, DEPRECATED, NEW, THIS, NIL, TRUE, FALSE, SIZEOF, IS, CLASS, VOID, CHAR_KEYWORD, INT_KEYWORD, BOOL, OBJECT, STRING_KEYWORD, SYMBOL_KEYWORD, MAPPING, ANY, MIXED, AUTO, OPERATOR, LET, IF, ELSE, WHILE, DO, FOR, FOREACH, SWITCH, CASE, DEFAULT, BREAK, CONTINUE, RETURN, TRY, CATCH -> {
                    style.setBold(true);
                    style.setForeground(Color.orange);
                }

                default -> style.setForeground(StyleConstants.getForeground(def));
            }
            setCharacterAttributes(token.begin(), token.end() - token.begin(), style.asStyle(def), true);
        }
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
    }
}
