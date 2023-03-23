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
import mhahnFr.SecretPathway.core.lpc.interpreter.highlight.Highlight;
import mhahnFr.SecretPathway.core.lpc.interpreter.highlight.MessagedHighlight;
import mhahnFr.SecretPathway.core.lpc.parser.Parser;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Token;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.Tokenizer;
import mhahnFr.SecretPathway.gui.editor.suggestions.Suggestion;
import mhahnFr.SecretPathway.gui.editor.theme.SPTheme;
import mhahnFr.utils.Pair;
import mhahnFr.utils.StringStream;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    /** The caret mover responsible for moving the caret.     */
    private CaretMover caretMover;
    /** The suggestion shower.                                */
    private SuggestionShower suggestionShower;
    /** Ignores an insertion.                                 */
    private Pair<Integer, String> ignore;
    /** The theme to be used for the syntax highlighting.     */
    private SPTheme theme = Settings.getInstance().getEditorTheme();
    /** The interpreted context of the source code.           */
    private volatile Context context;
    /** The ranges containing syntax errors.                  */
    private volatile List<Highlight<?>> highlights;
    /** The execution service for interpreting the code.      */
    private final ExecutorService thread = Executors.newSingleThreadExecutor();

    /**
     * Returns the currently used caret mover.
     *
     * @return the set caret mover
     * @see #setCaretMover(CaretMover)
     */
    public CaretMover getCaretMover() {
        return caretMover;
    }

    /**
     * Sets the caret mover to be called when the cursor
     * position should be updated.
     *
     * @param caretMover the new caret mover
     * @see #getCaretMover()
     */
    public void setCaretMover(final CaretMover caretMover) {
        this.caretMover = caretMover;
    }

    /**
     * Returns the currently set {@link SuggestionShower}.
     *
     * @return the suggestion shower
     */
    public SuggestionShower getSuggestionShower() {
        return suggestionShower;
    }

    /**
     * Sets the {@link SuggestionShower} to be used.
     *
     * @param suggestionShower the new suggestion shower
     */
    public void setSuggestionShower(final SuggestionShower suggestionShower) {
        this.suggestionShower = suggestionShower;
    }

    /**
     * Returns whether the character at the given offset
     * is a whitespace.
     *
     * @param offset the offset of the character
     * @return whether the character at the given offset is a whitespace
     * @throws BadLocationException if the position is negative
     * @see String#isBlank()
     */
    private boolean isWhitespace(final int offset) throws BadLocationException {
        if (offset >= getLength()) {
            return true;
        }
        return getText(offset, 1).isBlank();
    }

    /**
     * Returns the indentation of the previous line.
     *
     * @param offset the offset
     * @return the indentation of the previous line
     * @throws BadLocationException if the offset is out of bounds
     */
    private int getPreviousIndent(int offset) throws BadLocationException {
        int lineBegin = getLineBegin(offset);
        int indent;
        for (indent = 0; lineBegin < offset && getText(lineBegin, 1).equals(" "); ++lineBegin, ++indent);
        return indent;
    }

    /**
     * Returns whether the character at the previous position is
     * an opening parenthesis.
     *
     * @param offset the offset of the current character
     * @return whether the previous character is an opening parenthesis
     * @throws BadLocationException if the offset is out of bounds
     */
    private boolean isPreviousOpeningParenthesis(final int offset) throws BadLocationException {
        if (offset > 0) {
            final var c = getText(offset - 1, 1);
            return c.equals("(") ||
                   c.equals("{") ||
                   c.equals("[");
        } else {
            return false;
        }
    }

    /**
     * Returns whether the character at the given offset is
     * a closing parenthesis.
     *
     * @param offset the offset of the current character
     * @return whether the current character is a closing parenthesis
     * @throws BadLocationException if the offset is out of bounds
     */
    private boolean isClosingParenthesis(final int offset) throws BadLocationException {
        if (offset < getLength()) {
            final var c = getText(offset, 1);
            return c.equals(")") ||
                   c.equals("}") ||
                   c.equals("]");
        } else {
            return false;
        }
    }

    /**
     * Returns whether the given {@link String} contains only
     * spaces.
     *
     * @param string the string to be checked
     * @return whether the given string consists only of spaces
     */
    private boolean isSpaces(final String string) {
        for (final char c : string.toCharArray()) {
            if (c != ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the characters preceding the given position on
     * the same line are only spaces.
     *
     * @param offset the current position
     * @return whether only whitespaces precede the given position
     * @throws BadLocationException if the position is out of bounds
     */
    private boolean isOnlyWhitespacesOnLine(final int offset) throws BadLocationException {
        final var lineBegin = getLineBegin(offset);
        return isSpaces(getText(lineBegin, offset - lineBegin));
    }

    /**
     * Returns the character index of the beginning of the line
     * the given position is on.
     *
     * @param offset the position
     * @return the beginning position of the line
     * @throws BadLocationException if the position is out of bounds
     */
    private int getLineBegin(final int offset) throws BadLocationException {
        int lineBegin;
        for (lineBegin = offset > 0 ? offset - 1 : 0; lineBegin > 0 && !getText(lineBegin, 1).equals("\n"); --lineBegin);
        lineBegin = lineBegin > 0 ? ++lineBegin : 0;
        return lineBegin;
    }

    /**
     * Returns the corresponding closing string for the given one.
     *
     * @param opening the opening string
     * @return the according closing string
     */
    private String getClosingString(final String opening) {
        return switch (opening) {
            case "(" -> ")";
            case "{" -> "}";
            case "[" -> "]";

            default  -> opening;
        };
    }

    @Override
    public void insertString(int offs, final String str, final AttributeSet a) throws BadLocationException {
        int cursorDelta = 0;

        if (ignore != null && offs == ignore.getFirst() && str.equals(ignore.getSecond())) {
            ignore = null;
            caretMover.move(+1);
            return;
        } else {
            ignore = null;
        }

        boolean update = false,
                begin  = false,
                end    = true;
        final String insertion;
        switch (str) {
            case "\t" -> insertion = "    ";

            case "(", "{", "[", "\"", "'" -> {
                if (isWhitespace(offs)) {
                    final var closing = getClosingString(str);
                    insertion = str + closing;
                    cursorDelta = -1;
                    ignore = new Pair<>(offs + 1, closing);
                } else {
                    insertion = str;
                }
            }
            case "!" -> {
                if (getText(offs - 2, 2).equals("/*") && isWhitespace(offs)) {
                    insertion = "!!*/";
                    cursorDelta = -3;
                } else {
                    insertion = str;
                }
            }

            case "}" -> {
                if (isOnlyWhitespacesOnLine(offs)) {
                    final int lineBegin = getLineBegin(offs);
                    final var len = Math.min(offs - lineBegin, 4);
                    offs = offs - len;
                    remove(offs, len);
                }
                insertion = str;
            }

            case "\n" -> {
                final var openingParenthesis = isPreviousOpeningParenthesis(offs);
                if (openingParenthesis && isClosingParenthesis(offs)) {
                    final var indent = " ".repeat(getPreviousIndent(offs));
                    insertion = str + indent + "    \n" + indent;
                    cursorDelta = -indent.length() - 1;
                } else {
                    insertion = str + " ".repeat(getPreviousIndent(offs)) + (openingParenthesis ? "    " : "");
                }
            }

            default -> {
                if (str.contains("\n")) {
                    final var nlIndex = str.indexOf('\n');
                    insertion = str.substring(0, nlIndex + 1)
                              + str.substring(nlIndex + 1).indent(getPreviousIndent(offs));
                } else {
                    if (str.length() == 1 && !Tokenizer.isSpecial(str.charAt(0))) {
                        if (isSpecial(offs - 1) && isSpecial(offs)) {
                            begin = true;
                        }
                        update = true;
                        end = false;
                    }
                    insertion = str;
                }
            }
        }

        super.insertString(offs, insertion, a);
        if (suggestionShower != null) {
            if (update)   suggestionShower.updateSuggestions();
            if (begin)    suggestionShower.beginSuggestions();
            else if (end) suggestionShower.endSuggestions();
        }
        if (cursorDelta != 0 && caretMover != null) {
            caretMover.move(cursorDelta);
        }
        maybeUpdateHighlight();
    }

    /**
     * Returns whether the character found at the given offset is considered
     * to be special. If the offset is out of bounds, {@code true} is returned.
     *
     * @param offset the offset of the character to be checked
     * @return whether the found character is special
     */
    private boolean isSpecial(final int offset) {
        if (offset < 0 || offset > getLength()) return true;

        try {
            return Tokenizer.isSpecial(getText(offset, 1).charAt(0));
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the word that is found at the given position.
     * If no word is found, {@code null} is returned.
     *
     * @param at the position
     * @return the found word or {@code null}
     * @throws BadLocationException if the given position is out of bounds
     */
    public String getWord(final int at) throws BadLocationException {
        if (isInWord(at)) {
            final var wordBegin = getWordBegin(at);
            return getText(wordBegin, getWordEnd(at) - wordBegin);
        }
        return null;
    }

    /**
     * Returns whether the given position is in a word.
     * That includes the word following immediately to the
     * given position as well as immediately preceding the
     * given position.
     *
     * @param offs the position to be checked
     * @return whether the position is in or at a word
     * @throws BadLocationException if the given position is out of bounds
     */
    public boolean isInWord(final int offs) throws BadLocationException {
        return (offs > 0 && !Tokenizer.isSpecial(getText(offs - 1, 1).charAt(0))) ||
               (offs < getLength() && !Tokenizer.isSpecial(getText(offs, 1).charAt(0)));
    }

    /**
     * Returns the beginning position of the word found at the given
     * position. There must be a word at the given position, e. g. when
     * {@link #isInWord(int)} returns {@code true}.
     *
     * @param offs the position
     * @return the beginning position of the word
     * @throws BadLocationException if the position is out of bounds
     */
    public int getWordBegin(final int offs) throws BadLocationException {
        final var previousText = getText(0, offs);

        int begin;
        for (begin = offs - 1; begin > 0 && !Tokenizer.isSpecial(previousText.charAt(begin)); --begin);

        if (begin < 0 || Tokenizer.isSpecial(previousText.charAt(begin))) {
            ++begin;
        }
        return begin;
    }

    /**
     * Returns the end position of the word found at the given
     * position. There must be a word at the given position, e. g.
     * when {@link #isInWord(int)} returns {@link true}.
     *
     * @param offs the position
     * @return the end position of the word
     * @throws BadLocationException if the position is out of bounds
     */
    private int getWordEnd(final int offs) throws BadLocationException {
        final var nextText = getText(offs, getLength() - offs);

        int i;
        for (i = 0; i < nextText.length() && !Tokenizer.isSpecial(nextText.charAt(i)); ++i);

        if (i < nextText.length() && Tokenizer.isSpecial(nextText.charAt(i))) {
            --i;
        }

        return offs + i;
    }

    /**
     * Inserts the given suggestion and moves the cursor accordingly.
     *
     * @param offset      where to insert the suggestion
     * @param suggestion  the suggestion to be inserted
     * @param replaceWord whether to replace the current word
     * @throws BadLocationException if the position is out of bounds
     */
    public void insertSuggestion(int offset,
                                 final Suggestion suggestion,
                                 final boolean    replaceWord) throws BadLocationException {
        final var suggString = suggestion.getSuggestion();
        final var strLength  = suggString.length();

        final String str;
        if (isInWord(offset)) {
            final var word = getWord(offset);
            if (replaceWord || (!suggString.startsWith(word) && suggString.contains(word))) {
                final int begin = getWordBegin(offset),
                          end   = getWordEnd(offset);
                offset = begin;
                remove(begin, end - begin + (end < getLength() ? 1 : 0));
                str = suggString;
            } else {
                str = suggString.substring(offset - getWordBegin(offset));
            }
        } else {
            str = suggString;
        }
        final var indent = getPreviousIndent(offset);

        insertString(offset, str, null);

        final int toMove;
        if (suggestion.getRelativeCursorPosition() >= 0) {
            if (suggString.contains("\n")) {
                int nlCount = 0,
                    fpnl    = 0;
                for (int i = 0; i < strLength; ++i) {
                    if (suggString.charAt(i) == '\n') {
                        if (i < suggestion.getRelativeCursorPosition()) {
                            ++fpnl;
                        }
                        ++nlCount;
                    }
                }
                toMove = -(strLength + nlCount * indent) + suggestion.getRelativeCursorPosition() + fpnl * indent - 1;
            } else {
                toMove = -strLength + suggestion.getRelativeCursorPosition();
            }
        } else {
            toMove = 0;
        }
        if (toMove != 0 && caretMover != null) {
            caretMover.move(toMove);
        }
        if (suggestionShower != null) {
            suggestionShower.endSuggestions();
        }
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);

        if (suggestionShower != null && !isInWord(offs)) {
            suggestionShower.endSuggestions();
        }

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
                    setCharacterAttributes(range.getBegin(), range.getEnd() - range.getBegin(),
                            style.asStyle(def), false);
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
        return context.createSuggestions(position);
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
