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

public class JSONStyle {
    private Boolean bold;
    private Boolean italic;
    private Boolean strike;
    private Boolean underlined;
    private Integer size;
    private JSONColor background;
    private JSONColor foreground;

    public JSONStyle() {}

    public JSONStyle(final FStyle style) {
        bold       = style.isBold();
        italic     = style.isItalic();
        strike     = style.isStrikeThrough();
        underlined = style.isUnderlined();
        size       = style.getSize();
        background = new JSONColor(style.getBackground());
        foreground = new JSONColor(style.getForeground());
    }

    public FStyle getNative() {
        final var toReturn = new FStyle();

        toReturn.setForeground(foreground.getNative());
        toReturn.setBackground(background.getNative());
        toReturn.setBold(bold);
        toReturn.setItalic(italic);
        toReturn.setStrikeThrough(strike);
        toReturn.setUnderlined(underlined);
        toReturn.setSize(size);

        return toReturn;
    }

    public Boolean getBold() {
        return bold;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public Boolean getItalic() {
        return italic;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public Boolean getStrike() {
        return strike;
    }

    public void setStrike(Boolean strike) {
        this.strike = strike;
    }

    public Boolean getUnderlined() {
        return underlined;
    }

    public void setUnderlined(Boolean underlined) {
        this.underlined = underlined;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public JSONColor getBackground() {
        return background;
    }

    public void setBackground(JSONColor background) {
        this.background = background;
    }

    public JSONColor getForeground() {
        return foreground;
    }

    public void setForeground(JSONColor foreground) {
        this.foreground = foreground;
    }
}
