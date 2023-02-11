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

package mhahnFr.SecretPathway.gui.editor.theme.json;

import mhahnFr.utils.gui.abstraction.FStyle;

/**
 * This class is the data transfer model for styles.
 *
 * @author mhahnFr
 * @since 14.01.23
 */
public class JSONStyle {
    /** Whether to use a bold font.                        */
    private Boolean bold;
    /** Whether to use an italic font.                     */
    private Boolean italic;
    /** Whether to use a strike-through font.              */
    private Boolean strike;
    /** Whether to use an underlined font.                 */
    private Boolean underlined;
    /** The size of the font.                              */
    private Integer size;
    /** The background color.                              */
    private JSONColor background;
    /** The foreground color.                              */
    private JSONColor foreground;
    /** The string representation of the background color. */
    private String bg_rgb;
    /** The string representation of the foreground color. */
    private String fg_rgb;
    /** The name of this style.                            */
    private String name;

    /**
     * Default constructor. All values are zeroed.
     */
    public JSONStyle() {}

    /**
     * Copies the given {@link FStyle}. The inheritance is resolved.
     *
     * @param style the style to be copied
     */
    public JSONStyle(final FStyle style) {
        bold       = style.isBold();
        italic     = style.isItalic();
        strike     = style.isStrikeThrough();
        underlined = style.isUnderlined();
        size       = style.getSize();
        background = new JSONColor(style.getBackground());
        foreground = new JSONColor(style.getForeground());
    }

    /**
     * Returns a {@link FStyle} representation of this style.
     *
     * @return a "native" representation
     */
    public FStyle getNative() {
        final var toReturn = new FStyle();

        // TODO: Decode optional string colors
        toReturn.setForeground(foreground == null ? null : foreground.getNative());
        toReturn.setBackground(background == null ? null : background.getNative());
        toReturn.setBold(bold);
        toReturn.setItalic(italic);
        toReturn.setStrikeThrough(strike);
        toReturn.setUnderlined(underlined);
        toReturn.setSize(size);

        return toReturn;
    }

    /**
     * Returns whether to use a bold font.
     *
     * @return whether to use a bold font
     */
    public Boolean getBold() {
        return bold;
    }

    /**
     * Sets whether to use a bold font.
     *
     * @param bold whether to use a bold font
     */
    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    /**
     * Returns whether to use an italic font.
     *
     * @return whether to use an italic font
     */
    public Boolean getItalic() {
        return italic;
    }

    /**
     * Sets whether to use an italic font.
     *
     * @param italic whether to use an italic font
     */
    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    /**
     * Returns whether to use a strike-through font.
     *
     * @return whether to use a strike-through font
     */
    public Boolean getStrike() {
        return strike;
    }

    /**
     * Sets whether to use a strike-through font.
     *
     * @param strike whether to use a strike-through font
     */
    public void setStrike(Boolean strike) {
        this.strike = strike;
    }

    /**
     * Returns whether to use an underlined font.
     *
     * @return whether to use an underlined font
     */
    public Boolean getUnderlined() {
        return underlined;
    }

    /**
     * Sets whether to use an underlined font.
     *
     * @param underlined whether to use an underlined font
     */
    public void setUnderlined(Boolean underlined) {
        this.underlined = underlined;
    }

    /**
     * Returns the size of the font.
     *
     * @return the size of the font
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Sets the size of the font.
     *
     * @param size the size of the font
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Returns the background color.
     *
     * @return the background color
     */
    public JSONColor getBackground() {
        return background;
    }

    /**
     * Sets the background color.
     *
     * @param background the new background color
     */
    public void setBackground(JSONColor background) {
        this.background = background;
    }

    /**
     * Returns the foreground color.
     *
     * @return the foreground color
     */
    public JSONColor getForeground() {
        return foreground;
    }

    /**
     * Sets the foreground color.
     *
     * @param foreground the new foreground color
     */
    public void setForeground(JSONColor foreground) {
        this.foreground = foreground;
    }

    /**
     * Returns the name of this style.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name for this style.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the string representation of the background color.
     *
     * @return the background color as string
     */
    public String getBg_rgb() {
        return bg_rgb;
    }

    /**
     * Sets the background color using a string representation of it.
     *
     * @param bg_rgb the string representation
     */
    public void setBg_rgb(String bg_rgb) {
        this.bg_rgb = bg_rgb;
    }

    /**
     * Returns the string representation of the foreground color.
     *
     * @return the string representation
     */
    public String getFg_rgb() {
        return fg_rgb;
    }

    /**
     * Sets the foreground color using a string representation of it.
     *
     * @param fg_rgb the string representation
     */
    public void setFg_rgb(String fg_rgb) {
        this.fg_rgb = fg_rgb;
    }
}
