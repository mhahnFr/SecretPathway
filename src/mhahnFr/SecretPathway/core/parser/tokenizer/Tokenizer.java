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

package mhahnFr.SecretPathway.core.parser.tokenizer;

import mhahnFr.utils.StringStream;

import java.util.Stack;

/**
 * This class tokenizes a text into tokens.
 *
 * @author mhahnFr
 * @since 27.12.22
 */
public class Tokenizer {
    /** The stream of the source text.                      */
    private final StringStream stream;
    /** A stack containing pushed backed tokens.            */
    private final Stack<Token> pushbacks;
    /** Indicates whether comment tokens should be emitted. */
    private boolean commentTokens;

    /**
     * Constructs this Tokenizer using the given {@link StringStream}.
     *
     * @param stream the stream to read the source text from
     */
    public Tokenizer(StringStream stream) {
        this.stream = stream;
        this.pushbacks = new Stack<>();
    }

    /**
     * Pushes back the given token.
     *
     * @param token the token to be pushed back
     */
    public void pushback(final Token token) {
        pushbacks.push(token);
    }

    /**
     * Sets whether the tokenizer will emit comment tokens.
     *
     * @param enabled whether to emit comment tokens
     */
    public void setCommentTokensEnabled(final boolean enabled) {
        this.commentTokens = enabled;
    }

    /**
     * Returns the next token found in the stream.
     *
     * @return the next token found in the stream
     */
    public Token nextToken() {
        if (!pushbacks.isEmpty()) {
            return pushbacks.pop();
        }

        skipWhitespaces();

        if (!stream.hasNext()) {
            return new Token(stream.getIndex(), TokenType.EOF, null, stream.getIndex());
        } else if (stream.peek("/*!")) {
            return new Token(stream.getIndex(), TokenType.STRING, readTill("!*/", 3), stream.getIndex());
        } else if (stream.peek("/*")) {
            final var begin = stream.getIndex();
            final var comment = readTill("*/", 2);
            if (commentTokens) {
                return new Token(begin, TokenType.COMMENT_BLOCK, comment, stream.getIndex());
            } else {
                return nextToken();
            }
        } else if (stream.peek("//")) {
            final var begin = stream.getIndex();
            final var comment = readTill("\n", 2);
            if (commentTokens) {
                return new Token(begin, TokenType.COMMENT_LINE, comment, stream.getIndex());
            } else {
                return nextToken();
            }
        } else if (stream.peek('(')) {
            return new Token(stream.getIndex(), TokenType.LEFT_BRACKET, null, stream.skip(1));
        } else if (stream.peek(')')) {
            return new Token(stream.getIndex(), TokenType.RIGHT_BRACKET, null, stream.skip(1));
        } else if (stream.peek('[')) {
            return new Token(stream.getIndex(), TokenType.LEFT_BRACKET, null, stream.skip(1));
        } else if (stream.peek(']')) {
            return new Token(stream.getIndex(), TokenType.RIGHT_BRACKET, null, stream.skip(1));
        } else if (stream.peek('{')) {
            return new Token(stream.getIndex(), TokenType.LEFT_CURLY, null, stream.skip(1));
        } else if (stream.peek('}')) {
            return new Token(stream.getIndex(), TokenType.RIGHT_CURLY, null, stream.skip(1));
        } else if (stream.peek("...")) {
            return new Token(stream.getIndex(), TokenType.ELLIPSIS, null, stream.skip(3));
        } else if (stream.peek('.')) {
            return new Token(stream.getIndex(), TokenType.DOT, null, stream.skip(1));
        } else if (stream.peek(',')) {
            return new Token(stream.getIndex(), TokenType.COMMA, null, stream.skip(1));
        } else if (stream.peek("::")) {
            return new Token(stream.getIndex(), TokenType.SCOPE, null, stream.skip(2));
        } else if (stream.peek(':')) {
            return new Token(stream.getIndex(), TokenType.COLON, null, stream.skip(1));
        } else if (stream.peek(';')) {
            return new Token(stream.getIndex(), TokenType.SEMICOLON, null, stream.skip(1));
        } else if (stream.peek("==")) {
            return new Token(stream.getIndex(), TokenType.EQUALS, null, stream.skip(2));
        } else if (stream.peek("!=")) {
            return new Token(stream.getIndex(), TokenType.NOT_EQUAL, null, stream.skip(2));
        } else if (stream.peek("<<")) {
            return new Token(stream.getIndex(), TokenType.LEFT_SHIFT, null, stream.skip(2));
        } else if (stream.peek(">>")) {
            return new Token(stream.getIndex(), TokenType.RIGHT_SHIFT, null, stream.skip(2));
        } else if (stream.peek("<=")) {
            return new Token(stream.getIndex(), TokenType.LESS_OR_EQUAL, null, stream.skip(2));
        } else if (stream.peek('<')) {
            return new Token(stream.getIndex(), TokenType.LESS, null, stream.skip(1));
        } else if (stream.peek(">=")) {
            return new Token(stream.getIndex(), TokenType.GREATER_OR_EQUAL, null, stream.skip(2));
        } else if (stream.peek('>')) {
            return new Token(stream.getIndex(), TokenType.GREATER, null, stream.skip(1));
        } else if (stream.peek("&&")) {
            return new Token(stream.getIndex(), TokenType.AND, null, stream.skip(2));
        } else if (stream.peek("||")) {
            return new Token(stream.getIndex(), TokenType.OR, null, stream.skip(2));
        } else if (stream.peek('!')) {
            return new Token(stream.getIndex(), TokenType.NOT, null, stream.skip(1));
        } else if (stream.peek('=')) {
            return new Token(stream.getIndex(), TokenType.ASSIGNMENT, null, stream.skip(1));
        } else if (stream.peek("->")) {
            return new Token(stream.getIndex(), TokenType.ARROW, null, stream.skip(2));
        } else if (stream.peek("|->")) {
            return new Token(stream.getIndex(), TokenType.P_ARROW, null, stream.skip(3));
        } else if (stream.peek('&')) {
            return new Token(stream.getIndex(), TokenType.AMPERSAND, null, stream.skip(1));
        } else if (stream.peek('|')) {
            return new Token(stream.getIndex(), TokenType.PIPE, null, stream.skip(1));
        } else if (stream.peek("??")) {
            return new Token(stream.getIndex(), TokenType.DOUBLE_QUESTION, null, stream.skip(2));
        } else if (stream.peek('?')) {
            return new Token(stream.getIndex(), TokenType.QUESTION, null, stream.skip(1));
        } else if (stream.peek("+=")) {
            return new Token(stream.getIndex(), TokenType.ASSIGNMENT_PLUS, null, stream.skip(2));
        } else if (stream.peek("-=")) {
            return new Token(stream.getIndex(), TokenType.ASSIGNMENT_MINUS, null, stream.skip(2));
        } else if (stream.peek("*=")) {
            return new Token(stream.getIndex(), TokenType.ASSIGNMENT_STAR, null, stream.skip(2));
        } else if (stream.peek("/=")) {
            return new Token(stream.getIndex(), TokenType.ASSIGNMENT_SLASH, null, stream.skip(2));
        } else if (stream.peek("%=")) {
            return new Token(stream.getIndex(), TokenType.ASSIGNMENT_PERCENT, null, stream.skip(2));
        } else if (stream.peek("++")) {
            return new Token(stream.getIndex(), TokenType.INCREMENT, null, stream.skip(2));
        } else if (stream.peek("--")) {
            return new Token(stream.getIndex(), TokenType.DECREMENT, null, stream.skip(2));
        } else if (stream.peek('+')) {
            return new Token(stream.getIndex(), TokenType.PLUS, null, stream.skip(1));
        } else if (stream.peek('-')) {
            return new Token(stream.getIndex(), TokenType.MINUS, null, stream.skip(1));
        } else if (stream.peek('"')) {
            return new Token(stream.getIndex(), TokenType.STRING, readTill("\""), stream.getIndex());
        } else if (stream.peek('\'')) {
            return new Token(stream.getIndex(), TokenType.CHAR, readTill("'"), stream.getIndex());
        } else if (stream.peek("#'")) {
            return new Token(stream.getIndex(), TokenType.SYMBOL, readTill("'"), stream.getIndex());
        } else if (stream.peek("#:")) {
            return new Token(stream.getIndex(), TokenType.SYMBOL, readSymbol(), stream.getIndex());
        }
        // TODO: Keywords
        else if (stream.peek("new")) {
            return new Token(stream.getIndex(), TokenType.NEW, null, stream.skip(3));
        } else if (stream.peek("nil")) {
            return new Token(stream.getIndex(), TokenType.NIL, null, stream.skip(4));
        } else if (stream.peek("true")) {
            return new Token(stream.getIndex(), TokenType.TRUE, null, stream.skip(4));
        } else if (stream.peek("false")) {
            return new Token(stream.getIndex(), TokenType.FALSE, null, stream.skip(5));
        } else if (stream.peek("class")) {
            return new Token(stream.getIndex(), TokenType.CLASS, null, stream.skip(5));
        } else if (stream.peek("return")) {
            return new Token(stream.getIndex(), TokenType.RETURN, null, stream.skip(6));
        } else {
            final var position = stream.getIndex();
            final var buffer = new StringBuilder();

            while (stream.hasNext() && !isSpecial(stream.peek())) {
                buffer.append(stream.next());
            }

            final var string = buffer.toString();

            try {
                return new Token(position, TokenType.INT, Integer.decode(string), stream.getIndex());
            } catch (NumberFormatException ignored) {}

            return new Token(position, TokenType.IDENTIFIER, string, stream.getIndex());
        }
    }

    /**
     * Skips remaining whitespaces in the stream.
     */
    private void skipWhitespaces() {
        while (stream.hasNext() && Character.isWhitespace(stream.peek())) {
            stream.skip(1);
        }
    }

    private String readSymbol() {
        stream.skip(2);

        final var buffer = new StringBuilder();
        while (stream.hasNext() && !isSpecial(stream.peek())) {
            buffer.append(stream.next());
        }
        return buffer.toString();
    }

    private String readTill(final String string) {
        return readTill(string, 1);
    }

    /**
     * Reads a string from the stream until the given string has been
     * read or the end of the stream has been reached.
     *
     * @param string the string the read one should end with
     * @param skipping the amount of characters to be skipped
     * @return the read string
     */
    private String readTill(String string, final int skipping) {
        stream.skip(skipping);

        final var buffer = new StringBuilder();
        while (stream.hasNext() && !stream.peek(string)) {
            buffer.append(stream.next());
        }
        stream.skip(string.length());
        return buffer.toString();
    }

    /**
     * Returns whether the given character is a special one.
     *
     * @param c the character to be checked
     * @return whether the given character is special
     */
    private boolean isSpecial(char c) {
        return !(Character.isAlphabetic(c) || Character.isDigit(c) || c == '_');
    }
}
