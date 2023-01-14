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

import java.awt.Color;

/**
 * This class is the data transfer model for colors.
 *
 * @author mhahnFr
 * @since 14.01.23
 */
public class JSONColor {
    /** The red component of the color.   */
    private int red;
    /** The green component of the color. */
    private int green;
    /** The blue component of the color.  */
    private int blue;

    /**
     * Default constructor. All values are zeroed.
     */
    public JSONColor() {}

    /**
     * Copies the given color.
     *
     * @param color the color to be copied
     */
    public JSONColor(final Color color) {
        red   = color.getRed();
        green = color.getGreen();
        blue  = color.getBlue();
    }

    /**
     * Returns a {@link Color} representation of this color.
     *
     * @return a native representation of this color
     */
    public Color getNative() {
        return new Color(red, green, blue);
    }

    /**
     * Returns the red component of this color.
     *
     * @return the red component
     */
    public int getRed() {
        return red;
    }

    /**
     * Sets the red component of this color.
     *
     * @param red the red component
     */
    public void setRed(int red) {
        this.red = red;
    }

    /**
     * Returns the green component of this color.
     *
     * @return the green component
     */
    public int getGreen() {
        return green;
    }

    /**
     * Sets the green component of this color.
     *
     * @param green the green component
     */
    public void setGreen(int green) {
        this.green = green;
    }

    /**
     * Returns the blue component of this color.
     *
     * @return the blue component
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Sets the blue component of this color.
     *
     * @param blue the blue component
     */
    public void setBlue(int blue) {
        this.blue = blue;
    }
}
