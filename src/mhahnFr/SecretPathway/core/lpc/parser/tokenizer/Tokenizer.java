/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022 - 2023  mhahnFr
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

package mhahnFr.SecretPathway.core.lpc.parser.tokenizer;

import mhahnFr.utils.StringStream;

/**
 * This class tokenizes a text into tokens.
 *
 * @author mhahnFr
 * @since 27.12.22
 */
public class Tokenizer {
    /** The stream of the source text.                      */
    private final StringStream stream;
    /** Indicates whether comment tokens should be emitted. */
    private boolean commentTokens;

    /**
     * Constructs this Tokenizer using the given {@link StringStream}.
     *
     * @param stream the stream to read the source text from
     */
    public Tokenizer(StringStream stream) {
        this.stream = stream;
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
        skipWhitespaces();

        if (!stream.hasNext()) {
            return new Token(stream.getStreamPosition(), TokenType.EOF, null, stream.getStreamPosition());
        } else if (stream.peek("/*!")) {
            return new Token(stream.getStreamPosition(), TokenType.STRING, readTill("!*/", 3), stream.getStreamPosition());
        } else if (stream.peek("/*")) {
            final var begin = stream.getStreamPosition();
            final var comment = readTill("*/", 2);
            if (commentTokens) {
                return new Token(begin, TokenType.COMMENT_BLOCK, comment, stream.getStreamPosition());
            } else {
                return nextToken();
            }
        } else if (stream.peek("//")) {
            final var begin = stream.getStreamPosition();
            final var comment = readTill("\n", 2);
            if (commentTokens) {
                return new Token(begin, TokenType.COMMENT_LINE, comment, stream.getStreamPosition());
            } else {
                return nextToken();
            }
        }
        else if (stream.peek('('))   return new Token(stream.getStreamPosition(), TokenType.LEFT_PAREN,         null, stream.skip());
        else if (stream.peek(')'))   return new Token(stream.getStreamPosition(), TokenType.RIGHT_PAREN,        null, stream.skip());
        else if (stream.peek('['))   return new Token(stream.getStreamPosition(), TokenType.LEFT_BRACKET,       null, stream.skip());
        else if (stream.peek(']'))   return new Token(stream.getStreamPosition(), TokenType.RIGHT_BRACKET,      null, stream.skip());
        else if (stream.peek('{'))   return new Token(stream.getStreamPosition(), TokenType.LEFT_CURLY,         null, stream.skip());
        else if (stream.peek('}'))   return new Token(stream.getStreamPosition(), TokenType.RIGHT_CURLY,        null, stream.skip());
        else if (stream.peek("...")) return new Token(stream.getStreamPosition(), TokenType.ELLIPSIS,           null, stream.skip(3));
        else if (stream.peek(".."))  return new Token(stream.getStreamPosition(), TokenType.RANGE,              null, stream.skip(2));
        else if (stream.peek('.'))   return new Token(stream.getStreamPosition(), TokenType.DOT,                null, stream.skip());
        else if (stream.peek(','))   return new Token(stream.getStreamPosition(), TokenType.COMMA,              null, stream.skip());
        else if (stream.peek("::"))  return new Token(stream.getStreamPosition(), TokenType.SCOPE,              null, stream.skip(2));
        else if (stream.peek(':'))   return new Token(stream.getStreamPosition(), TokenType.COLON,              null, stream.skip());
        else if (stream.peek(';'))   return new Token(stream.getStreamPosition(), TokenType.SEMICOLON,          null, stream.skip());
        else if (stream.peek("=="))  return new Token(stream.getStreamPosition(), TokenType.EQUALS,             null, stream.skip(2));
        else if (stream.peek("!="))  return new Token(stream.getStreamPosition(), TokenType.NOT_EQUAL,          null, stream.skip(2));
        else if (stream.peek("<<"))  return new Token(stream.getStreamPosition(), TokenType.LEFT_SHIFT,         null, stream.skip(2));
        else if (stream.peek(">>"))  return new Token(stream.getStreamPosition(), TokenType.RIGHT_SHIFT,        null, stream.skip(2));
        else if (stream.peek("<="))  return new Token(stream.getStreamPosition(), TokenType.LESS_OR_EQUAL,      null, stream.skip(2));
        else if (stream.peek('<'))   return new Token(stream.getStreamPosition(), TokenType.LESS,               null, stream.skip());
        else if (stream.peek(">="))  return new Token(stream.getStreamPosition(), TokenType.GREATER_OR_EQUAL,   null, stream.skip(2));
        else if (stream.peek('>'))   return new Token(stream.getStreamPosition(), TokenType.GREATER,            null, stream.skip());
        else if (stream.peek("&&"))  return new Token(stream.getStreamPosition(), TokenType.AND,                null, stream.skip(2));
        else if (stream.peek("||"))  return new Token(stream.getStreamPosition(), TokenType.OR,                 null, stream.skip(2));
        else if (stream.peek('!'))   return new Token(stream.getStreamPosition(), TokenType.NOT,                null, stream.skip());
        else if (stream.peek('='))   return new Token(stream.getStreamPosition(), TokenType.ASSIGNMENT,         null, stream.skip());
        else if (stream.peek("->"))  return new Token(stream.getStreamPosition(), TokenType.ARROW,              null, stream.skip(2));
        else if (stream.peek('&'))   return new Token(stream.getStreamPosition(), TokenType.AMPERSAND,          null, stream.skip());
        else if (stream.peek('|'))   return new Token(stream.getStreamPosition(), TokenType.PIPE,               null, stream.skip());
        else if (stream.peek("??"))  return new Token(stream.getStreamPosition(), TokenType.DOUBLE_QUESTION,    null, stream.skip(2));
        else if (stream.peek('?'))   return new Token(stream.getStreamPosition(), TokenType.QUESTION,           null, stream.skip());
        else if (stream.peek("+="))  return new Token(stream.getStreamPosition(), TokenType.ASSIGNMENT_PLUS,    null, stream.skip(2));
        else if (stream.peek("-="))  return new Token(stream.getStreamPosition(), TokenType.ASSIGNMENT_MINUS,   null, stream.skip(2));
        else if (stream.peek("*="))  return new Token(stream.getStreamPosition(), TokenType.ASSIGNMENT_STAR,    null, stream.skip(2));
        else if (stream.peek("/="))  return new Token(stream.getStreamPosition(), TokenType.ASSIGNMENT_SLASH,   null, stream.skip(2));
        else if (stream.peek("%="))  return new Token(stream.getStreamPosition(), TokenType.ASSIGNMENT_PERCENT, null, stream.skip(2));
        else if (stream.peek("++"))  return new Token(stream.getStreamPosition(), TokenType.INCREMENT,          null, stream.skip(2));
        else if (stream.peek("--"))  return new Token(stream.getStreamPosition(), TokenType.DECREMENT,          null, stream.skip(2));
        else if (stream.peek('+'))   return new Token(stream.getStreamPosition(), TokenType.PLUS,               null, stream.skip());
        else if (stream.peek('-'))   return new Token(stream.getStreamPosition(), TokenType.MINUS,              null, stream.skip());
        else if (stream.peek('*'))   return new Token(stream.getStreamPosition(), TokenType.STAR,               null, stream.skip());
        else if (stream.peek('/'))   return new Token(stream.getStreamPosition(), TokenType.SLASH,              null, stream.skip());
        else if (stream.peek('%'))   return new Token(stream.getStreamPosition(), TokenType.PERCENT,            null, stream.skip());
        else if (stream.peek('"'))   return new Token(stream.getStreamPosition(), TokenType.STRING,    readTill("\""),   stream.getStreamPosition());
        else if (stream.peek('\''))  return new Token(stream.getStreamPosition(), TokenType.CHARACTER, readTill("'"),    stream.getStreamPosition());
        else if (stream.peek("#'"))  return new Token(stream.getStreamPosition(), TokenType.SYMBOL,    readTill("'", 2), stream.getStreamPosition());
        else if (stream.peek("#:"))  return new Token(stream.getStreamPosition(), TokenType.SYMBOL,    readSymbol(),     stream.getStreamPosition());

        return nextWord();
    }

    /**
     * Constructs and returns a token from the next word read in the stream.
     *
     * @return a token constructed from the next read word
     */
    private Token nextWord() {
        final var begin = stream.getStreamPosition();
        final var word  = readWord();
        final var end   = stream.getStreamPosition();

        switch (word) {
            case "#include"   -> { return new Token(begin, TokenType.INCLUDE,        null, end); }
            case "inherit"    -> { return new Token(begin, TokenType.INHERIT,        null, end); }
            case "private"    -> { return new Token(begin, TokenType.PRIVATE,        null, end); }
            case "protected"  -> { return new Token(begin, TokenType.PROTECTED,      null, end); }
            case "public"     -> { return new Token(begin, TokenType.PUBLIC,         null, end); }
            case "override"   -> { return new Token(begin, TokenType.OVERRIDE,       null, end); }
            case "deprecated" -> { return new Token(begin, TokenType.DEPRECATED,     null, end); }
            case "nosave"     -> { return new Token(begin, TokenType.NOSAVE,         null, end); }
            case "new"        -> { return new Token(begin, TokenType.NEW,            null, end); }
            case "this"       -> { return new Token(begin, TokenType.THIS,           null, end); }
            case "nil"        -> { return new Token(begin, TokenType.NIL,            null, end); }
            case "true"       -> { return new Token(begin, TokenType.TRUE,           null, end); }
            case "false"      -> { return new Token(begin, TokenType.FALSE,          null, end); }
            case "sizeof"     -> { return new Token(begin, TokenType.SIZEOF,         null, end); }
            case "is"         -> { return new Token(begin, TokenType.IS,             null, end); }
            case "class"      -> { return new Token(begin, TokenType.CLASS,          null, end); }
            case "void"       -> { return new Token(begin, TokenType.VOID,           null, end); }
            case "char"       -> { return new Token(begin, TokenType.CHAR_KEYWORD,   null, end); }
            case "int"        -> { return new Token(begin, TokenType.INT_KEYWORD,    null, end); }
            case "bool"       -> { return new Token(begin, TokenType.BOOL,           null, end); }
            case "object"     -> { return new Token(begin, TokenType.OBJECT,         null, end); }
            case "string"     -> { return new Token(begin, TokenType.STRING_KEYWORD, null, end); }
            case "symbol"     -> { return new Token(begin, TokenType.SYMBOL_KEYWORD, null, end); }
            case "mapping"    -> { return new Token(begin, TokenType.MAPPING,        null, end); }
            case "any"        -> { return new Token(begin, TokenType.ANY,            null, end); }
            case "mixed"      -> { return new Token(begin, TokenType.MIXED,          null, end); }
            case "auto"       -> { return new Token(begin, TokenType.AUTO,           null, end); }
            case "let"        -> { return new Token(begin, TokenType.LET,            null, end); }
            case "if"         -> { return new Token(begin, TokenType.IF,             null, end); }
            case "else"       -> { return new Token(begin, TokenType.ELSE,           null, end); }
            case "while"      -> { return new Token(begin, TokenType.WHILE,          null, end); }
            case "do"         -> { return new Token(begin, TokenType.DO,             null, end); }
            case "foreach"    -> { return new Token(begin, TokenType.FOREACH,        null, end); }
            case "for"        -> { return new Token(begin, TokenType.FOR,            null, end); }
            case "switch"     -> { return new Token(begin, TokenType.SWITCH,         null, end); }
            case "case"       -> { return new Token(begin, TokenType.CASE,           null, end); }
            case "default"    -> { return new Token(begin, TokenType.DEFAULT,        null, end); }
            case "break"      -> { return new Token(begin, TokenType.BREAK,          null, end); }
            case "continue"   -> { return new Token(begin, TokenType.CONTINUE,       null, end); }
            case "return"     -> { return new Token(begin, TokenType.RETURN,         null, end); }
            case "try"        -> { return new Token(begin, TokenType.TRY,            null, end); }
            case "catch"      -> { return new Token(begin, TokenType.CATCH,          null, end); }
            case "operator"   -> { return new Token(begin, TokenType.OPERATOR,       null, end); }
        }

        try {
            return new Token(begin, TokenType.INTEGER, Integer.decode(word), end);
        } catch (NumberFormatException e) {
            return new Token(begin, TokenType.IDENTIFIER, word, end);
        }
    }

    /**
     * Reads and returns a word from the stream.
     *
     * @return the read word
     */
    private String readWord() {
        final var buffer = new StringBuilder();
        while (stream.hasNext() && !isSpecial(stream.peek())) {
            buffer.append(stream.next());
        }
        if (buffer.isEmpty() && stream.hasNext()) {
            // Unrecognized character used as word to prevent endless loop
            buffer.append(stream.next());
        }
        return buffer.toString();
    }

    /**
     * Skips remaining whitespaces in the stream.
     */
    private void skipWhitespaces() {
        while (stream.hasNext() && Character.isWhitespace(stream.peek())) {
            stream.skip();
        }
    }

    /**
     * Reads and returns a symbol name from the stream.
     *
     * @return the read symbol name
     */
    private String readSymbol() {
        stream.skip(2);

        final var buffer = new StringBuilder();
        while (stream.hasNext() && !isSpecial(stream.peek())) {
            buffer.append(stream.next());
        }
        return buffer.toString();
    }

    /**
     * Reads a string from the stream until the given string has been
     * read or the end of the stream has been reached.
     *
     * @param string the string the read one should end with
     * @return the read string
     */
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
        char previous     = '\0',
             overPrevious = '\0';
        while (stream.hasNext() && !(stream.peek(string) && (previous != '\\' || overPrevious == '\\'))) {
            overPrevious = previous;
            previous     = stream.next();

            buffer.append(previous);
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
    public static boolean isSpecial(char c) {
        return !(Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '$' || c == '#');
    }
}
