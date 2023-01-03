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

package mhahnFr.SecretPathway.core.protocols.ansi;

import mhahnFr.SecretPathway.core.net.ConnectionSender;
import mhahnFr.SecretPathway.core.protocols.ProtocolPlugin;
import mhahnFr.SecretPathway.gui.ConnectionDelegate;
import mhahnFr.utils.ByteHelper;
import mhahnFr.utils.gui.abstraction.FStyle;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

/**
 * This class acts as a plugin for ANSI escape codes.
 *
 * @author mhahnFr
 * @since 03.01.23
 */
public class ANSIPlugin implements ProtocolPlugin {
    /** The buffer for the incoming ANSI escape sequence.            */
    private final List<Byte> buffer = new Vector<>();
    /** The owner of this plugin, used for altering the text styles. */
    private final ConnectionDelegate owner;

    /**
     * Initializes this plugin using the given owner.
     *
     * @param owner the owner of this plugin
     */
    public ANSIPlugin(ConnectionDelegate owner) {
        this.owner = owner;
    }

    @Override
    public boolean isBegin(byte b) {
        return b == 0x1B;
    }

    @Override
    public boolean process(byte b, ConnectionSender sender) {
        if (b == 0x6D) {
            parseBuffer();
            buffer.clear();
            return false;
        }
        buffer.add(b);
        return true;
    }

    /**
     * Calculates the colour cube value using the given colour code
     * and the given code.
     *
     * @param color the colour code
     * @param code the code used for the calculations
     * @return the colour cube value
     */
    private int color256CubeCalc(int color, int code) {
        final var tmp = ((color - 16) / code) % 6;
        return tmp == 0 ? 0 :
                (14135 + 10280 * tmp) / 256;
    }

    /**
     * Returns the {@link Color} decoded from the given colour code.
     * If it cannot be decoded, {@code null} is returned.
     *
     * @param colourCode the code of the 256-bit colour cube
     * @return the decoded colour
     */
    private Color colourFrom256Bit(int colourCode) {
        Color result = null;

        if (colourCode < 16) {
            switch (colourCode) {
                case 0  -> result = new Color(0,   0,   0);
                case 1  -> result = new Color(192, 0,   0);
                case 2  -> result = new Color(0,   192, 0);
                case 3  -> result = new Color(192, 192, 0);
                case 4  -> result = new Color(0,   0,   192);
                case 5  -> result = new Color(192, 0,   192);
                case 6  -> result = new Color(0,   192, 192);
                case 7  -> result = new Color(192, 192, 192);
                case 8  -> result = new Color(128, 128, 128);
                case 9  -> result = new Color(255, 0,   0);
                case 10 -> result = new Color(0,   255, 0);
                case 11 -> result = new Color(255, 255, 0);
                case 12 -> result = new Color(0,   0,   255);
                case 13 -> result = new Color(255, 0,   255);
                case 14 -> result = new Color(0,   255, 255);
                case 15 -> result = new Color(255, 255, 255);
            }
        } else if (colourCode < 232) {
            result = new Color(color256CubeCalc(colourCode, 36),
                    color256CubeCalc(colourCode, 6),
                    color256CubeCalc(colourCode, 1));
        } else if (colourCode < 256) {
            final var value = ((float) ((2056 + 2570 * (colourCode - 232))) / 256) / 255;
            result = new Color(value, value, value);
        }

        return result;
    }

    /**
     * Parses the contents of the given ANSI buffer. If the buffer could not be parsed
     * properly, {@code false} is returned and the current style is
     * left untouched.
     *
     * @return whether the buffer was parsed successfully
     */
    private boolean parseBuffer() {
        var str = new String(ByteHelper.castToByte(buffer.toArray(new Byte[0])), StandardCharsets.US_ASCII);

        var result  = true;
        var current = owner.getCurrentStyle();

        final var before = new FStyle(current, false);

        try {
            str = str.substring(1);
            final var splits = str.split(";");
            for (int i = 0; i < splits.length; ++i) {
                switch (Integer.parseInt(splits[i])) {
                    case 0  -> current = new FStyle();
                    case 1  -> current.setBold(true);
                    case 3  -> current.setItalic(true);
                    case 4  -> current.setUnderlined(true);
                    case 21 -> current.setBold(false);
                    case 23 -> current.setItalic(false);
                    case 24 -> current.setUnderlined(false);

                    // Foreground
                    case 30 -> current.setForeground(Color.black);
                    case 31 -> current.setForeground(new Color(192, 0,   0));
                    case 32 -> current.setForeground(new Color(0,   192, 0));
                    case 33 -> current.setForeground(new Color(192, 192, 0));
                    case 34 -> current.setForeground(new Color(0,   0,   192));
                    case 35 -> current.setForeground(new Color(192, 0,   192));
                    case 36 -> current.setForeground(new Color(0,   192, 192));
                    case 37 -> current.setForeground(Color.lightGray);
                    case 39 -> current.setForeground(null);
                    case 90 -> current.setForeground(Color.darkGray);
                    case 91 -> current.setForeground(new Color(255, 0,   0));
                    case 92 -> current.setForeground(new Color(0,   255, 0));
                    case 93 -> current.setForeground(new Color(255, 255, 0));
                    case 94 -> current.setForeground(new Color(0,   0,   255));
                    case 95 -> current.setForeground(new Color(255, 0,   255));
                    case 96 -> current.setForeground(new Color(0,   255, 255));
                    case 97 -> current.setForeground(Color.white);

                    // Background
                    case 40  -> current.setBackground(Color.black);
                    case 41  -> current.setBackground(new Color(192, 0,   0));
                    case 42  -> current.setBackground(new Color(0,   192, 0));
                    case 43  -> current.setBackground(new Color(192, 192, 0));
                    case 44  -> current.setBackground(new Color(0,   0,   192));
                    case 45  -> current.setBackground(new Color(192, 0,   192));
                    case 46  -> current.setBackground(new Color(0,   192, 192));
                    case 47  -> current.setBackground(Color.lightGray);
                    case 49  -> current.setBackground(null);
                    case 100 -> current.setBackground(Color.darkGray);
                    case 101 -> current.setBackground(new Color(255, 0,   0));
                    case 102 -> current.setBackground(new Color(0,   255, 0));
                    case 103 -> current.setBackground(new Color(255, 255, 0));
                    case 104 -> current.setBackground(new Color(0,   0,   255));
                    case 105 -> current.setBackground(new Color(255, 0,   255));
                    case 106 -> current.setBackground(new Color(0,   255, 255));
                    case 107 -> current.setBackground(Color.white);

                    case 38 -> {
                        ++i;

                        final var code = Integer.parseInt(splits[i]);
                        if (code == 5) {
                            current.setForeground(colourFrom256Bit(Integer.parseInt(splits[i + 1])));
                            ++i;
                        } else if (code == 2) {
                            current.setForeground(new Color(Integer.parseInt(splits[i + 1]),
                                    Integer.parseInt(splits[i + 2]),
                                    Integer.parseInt(splits[i + 3])));
                            i += 3;
                        }
                    }

                    case 48 -> {
                        ++i;

                        final var code = Integer.parseInt(splits[i]);
                        if (code == 5) {
                            current.setBackground(colourFrom256Bit(Integer.parseInt(splits[i + 1])));
                            ++i;
                        } else if (code == 2) {
                            current.setBackground(new Color(Integer.parseInt(splits[i + 1]),
                                    Integer.parseInt(splits[i + 2]),
                                    Integer.parseInt(splits[i + 3])));
                            i += 3;
                        }
                    }

                    default -> System.err.println("Code not supported: " + splits[i] + "!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            current = before;
            result = false;
        }
        owner.setCurrentStyle(current);
        return result;
    }
}
