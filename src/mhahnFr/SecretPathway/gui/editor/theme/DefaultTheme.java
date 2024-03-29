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

package mhahnFr.SecretPathway.gui.editor.theme;

import mhahnFr.SecretPathway.core.lpc.interpreter.InterpretationType;
import mhahnFr.SecretPathway.core.lpc.interpreter.highlight.HighlightType;
import mhahnFr.SecretPathway.core.lpc.parser.ast.ASTType;
import mhahnFr.SecretPathway.core.lpc.parser.tokenizer.TokenType;
import mhahnFr.utils.gui.abstraction.FStyle;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a default theme for the syntax highlighting.
 *
 * @author mhahnFr
 * @since 10.01.23
 */
public class DefaultTheme implements SPTheme {
    private final Map<HighlightType, FStyle> styles = new HashMap<>();

    /**
     * Initializes this default theme.
     */
    public DefaultTheme() {
        final var keyword = new FStyle();
        keyword.setBold(true);
        keyword.setForeground(new Color(0x046382));

        final var id = new FStyle();
        id.setForeground(new Color(0xb30bbf));

        final var type = new FStyle();
        type.setForeground(new Color(0x048282));

        final var flow = new FStyle();
        flow.setForeground(new Color(0x167505));

        final var constant = new FStyle();
        constant.setForeground(new Color(0xaba003));

        final var control = new FStyle();
        control.setForeground(new Color(0xB60117));

        final var comment = new FStyle();
        comment.setItalic(true);
        comment.setForeground(Color.gray);

        final var warning = new FStyle();
        warning.setForeground(new Color(192, 192, 0));
        warning.setUnderlined(true);

        styles.put(InterpretationType.WARNING, warning);
        styles.put(InterpretationType.NOT_FOUND_BUILTIN, warning);

        styles.put(TokenType.IDENTIFIER, id);

        styles.put(TokenType.NIL,   keyword);
        styles.put(TokenType.TRUE,  keyword);
        styles.put(TokenType.FALSE, keyword);

        styles.put(TokenType.INCLUDE,    control);
        styles.put(TokenType.INHERIT,    control);
        styles.put(TokenType.PRIVATE,    control);
        styles.put(TokenType.PROTECTED,  control);
        styles.put(TokenType.PUBLIC,     control);
        styles.put(TokenType.OVERRIDE,   control);
        styles.put(TokenType.DEPRECATED, control);
        styles.put(TokenType.NOSAVE,     control);
        styles.put(TokenType.NEW,        control);
        styles.put(TokenType.THIS,       control);
        styles.put(TokenType.SIZEOF,     control);
        styles.put(TokenType.IS,         control);
        styles.put(TokenType.CLASS,      control);

        styles.put(TokenType.IF,       flow);
        styles.put(TokenType.ELSE,     flow);
        styles.put(TokenType.WHILE,    flow);
        styles.put(TokenType.DO,       flow);
        styles.put(TokenType.FOR,      flow);
        styles.put(TokenType.FOREACH,  flow);
        styles.put(TokenType.SWITCH,   flow);
        styles.put(TokenType.CASE,     flow);
        styles.put(TokenType.DEFAULT,  flow);
        styles.put(TokenType.BREAK,    flow);
        styles.put(TokenType.CONTINUE, flow);
        styles.put(TokenType.RETURN,   flow);
        styles.put(TokenType.TRY,      flow);
        styles.put(TokenType.CATCH,    flow);

        styles.put(TokenType.VOID,           type);
        styles.put(TokenType.CHAR_KEYWORD,   type);
        styles.put(TokenType.INT_KEYWORD,    type);
        styles.put(TokenType.BOOL,           type);
        styles.put(TokenType.OBJECT,         type);
        styles.put(TokenType.STRING_KEYWORD, type);
        styles.put(TokenType.SYMBOL_KEYWORD, type);
        styles.put(TokenType.MAPPING,        type);
        styles.put(TokenType.ANY,            type);
        styles.put(TokenType.MIXED,          type);
        styles.put(TokenType.AUTO,           type);
        styles.put(TokenType.OPERATOR,       type);
        styles.put(TokenType.LET,            type);

        styles.put(TokenType.INTEGER,   constant);
        styles.put(TokenType.STRING,    constant);
        styles.put(TokenType.CHARACTER, constant);
        styles.put(TokenType.SYMBOL,    constant);

        styles.put(TokenType.COMMENT_BLOCK, comment);
        styles.put(TokenType.COMMENT_LINE,  comment);

        final var errorStyle = new FStyle();
        errorStyle.setForeground(Color.red);
        errorStyle.setBold(true);

        styles.put(ASTType.WRONG, errorStyle);

        final var errorStyle2 = new FStyle();
        errorStyle2.setForeground(Color.red);
        errorStyle2.setUnderlined(true);

        styles.put(InterpretationType.ERROR, errorStyle2);
        styles.put(InterpretationType.TYPE_MISMATCH, errorStyle2);

        final var errorStyle3 = new FStyle();
        errorStyle3.setForeground(Color.red);
        styles.put(InterpretationType.NOT_FOUND, errorStyle3);

        final var missingStyle = new FStyle();
        missingStyle.setBackground(new Color(255, 130, 130));

        styles.put(ASTType.MISSING, missingStyle);
    }

    @Override
    public FStyle styleFor(HighlightType highlightType) {
        return styles.get(highlightType);
    }
}
