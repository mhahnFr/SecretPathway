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

package mhahnFr.SecretPathway.core;

import javax.swing.KeyStroke;
import java.awt.Font;
import java.awt.event.KeyEvent;

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

    /**
     * Constants for use with actions.
     */
    public static final class Actions {
        /** Action command for sending a message.                       */
        public static final String SEND           = "send";
        /** Action command for closing a connection.                    */
        public static final String CLOSE          = "close";
        /** Action command for reconnecting to the connection endpoint. */
        public static final String RECONNECT      = "reconnect";
        /** Action command for creating a new connection.               */
        public static final String NEW            = "new";
        /** Action command used for opening the editor.                 */
        public static final String OPEN_EDITOR    = "open_editor";
    }

    /**
     * Constants for use in the user interface.
     */
    public static final class UI {
        /** The default monospaced font to be used. */
        public static final Font FONT = new Font("monospaced", Font.PLAIN, 12);
    }

    /**
     * Constants for use with the editor.
     */
    public static final class Editor {
        /** The string displayed for the default theme.               */
        public static final String DEFAULT_THEME = "Default";
        /** The string displayed for choosing a theme.                */
        public static final String CHOOSE_THEME = "Choose...";
        /** The keystroke for toggling the suggestions window.        */
        public static final KeyStroke SHOW_SUGGESTIONS = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        /** The keystroke for selecting the next suggestion.          */
        public static final KeyStroke POPUP_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        /** The keystroke for selecting the previous suggestion.      */
        public static final KeyStroke POPUP_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        /** The keystroke for closing the suggestions window.         */
        public static final KeyStroke POPUP_CLOSE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        /** The keystroke for taking the selected suggestion.         */
        public static final KeyStroke POPUP_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        /** The keystroke for replacing with the selected suggestion. */
        public static final KeyStroke POPUP_REPLACE = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    }
}
