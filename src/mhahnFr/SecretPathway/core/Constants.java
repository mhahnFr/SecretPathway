/*
 * SecretPathway - A MUD client.
 *
 * Copyright (C) 2022  mhahnFr
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

package mhahnFr.SecretPathway.core;

import java.awt.Font;

/**
 * This class contains some constant values used in the whole project.
 *
 * @since 02.11.2022
 * @author mhahnFr
 */
public final class Constants {
    /** The name to be used inside the project. */
    public static final String NAME    = "SecretPathway";
    /** The version as String.                  */
    public static final String VERSION = "1.0";

    public static final class Actions {
        public static final String SEND = "send";
    }

    public static final class UI {
        public static final Font FONT = new Font("monospaced", Font.PLAIN, 12);
    }
}
