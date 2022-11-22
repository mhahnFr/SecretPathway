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

import mhahnFr.SecretPathway.CLI;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This class provides a singleton using which the settings and states of
 * the application can be stored. Access the singleton using {@link Settings#getInstance()}.
 *
 * @since 31.10.2022
 * @author mhahnFr
 */
public final class Settings {
    /** The one and only instance of this class. */
    private static final Settings instance = new Settings();

    /** The {@link Preferences} instance.       */
    private final Preferences preferences;

    /**
     * Constructs this settings object.
     */
    private Settings() {
        preferences = Preferences.userNodeForPackage(CLI.class);
    }

    /**
     * Returns the saved hostname or {@code null} if not set.
     *
     * @return the saved hostname
     */
    public String getHostname() {
        return preferences.get(Keys.HOSTNAME, null);
    }

    /**
     * Returns the saved port number or {@code -1} if not set.
     *
     * @return the saved port
     */
    public int getPort() {
        return preferences.getInt(Keys.PORT, -1);
    }

    /**
     * Returns the saved width of the main window or -1 if not set.
     *
     * @return the saved width of the main window
     */
    public int getWindowWidth() {
        return preferences.getInt(Keys.WINDOW_WIDTH, -1);
    }

    /**
     * Returns the saved height of the main window or -1 if not set.
     *
     * @return the saved height of the main window
     */
    public int getWindowHeight() {
        return preferences.getInt(Keys.WINDOW_HEIGHT, -1);
    }

    /**
     * Returns the saved X-coordinate of the window location or -1 if not set.
     *
     * @return the saved X-coordinate of the window
     */
    public int getWindowLocationX() {
        return preferences.getInt(Keys.WINDOW_LOCATION_X, -1);
    }

    /**
     * Returns the saved Y-coordinate of the window location or -1 if not set.
     *
     * @return the saved Y-coordinate of the window
     */
    public int getWindowLocationY() {
        return preferences.getInt(Keys.WINDOW_LOCATION_Y, -1);
    }

    /**
     * Returns the stored font size.
     *
     * @return the store font size
     */
    public int getFontSize() {
        return preferences.getInt(Keys.FONT_SIZE, 12);
    }

    /**
     * Returns whether the dark mode should be enabled.
     *
     * @return whether the dark mode is enabled
     */
    public boolean getDarkMode() {
        return preferences.getInt(Keys.DARK_MODE, 0) != 0;
    }

    /**
     * Stores the given hostname.
     *
     * @param hostname the hostname to store
     */
    public Settings setHostname(String hostname) {
        preferences.put(Keys.HOSTNAME, hostname);
        return this;
    }

    /**
     * Stores the given port number.
     *
     * @param port the port number to store
     */
    public Settings setPort(int port) {
        preferences.putInt(Keys.PORT, port);
        return this;
    }

    /**
     * Stores the window location.
     *
     * @param x the X-coordinate of the window position
     * @param y the Y-coordinate of the window postion
     */
    public Settings setWindowLocation(int x, int y) {
        preferences.putInt(Keys.WINDOW_LOCATION_X, x);
        preferences.putInt(Keys.WINDOW_LOCATION_Y, y);
        return this;
    }

    /**
     * Stores the window size.
     *
     * @param width the width of the window
     * @param height the height of the window
     */
    public Settings setWindowSize(int width, int height) {
        preferences.putInt(Keys.WINDOW_WIDTH, width);
        preferences.putInt(Keys.WINDOW_HEIGHT, height);
        return this;
    }

    /**
     * Sets the font size.
     *
     * @param size the new font size
     * @return this instance
     */
    public Settings setFontSize(int size) {
        preferences.putInt(Keys.FONT_SIZE, size);
        return this;
    }

    /**
     * Sets whether the dark mode should be enabled.
     *
     * @param enabled whether the dark mode is enabled
     * @return this instance
     */
    public Settings setDarkMode(boolean enabled) {
        preferences.putInt(Keys.DARK_MODE, enabled ? 1 : 0);
        return this;
    }

    /**
     * Attempts to flush the underlying {@link Preferences}. Returns whether the
     * operation was successful.
     *
     * @return whether the settings were flushed successfully
     */
    public boolean flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            return false;
        }
        return true;
    }

    /**
     * Removes all previously stored settings.
     *
     * @return whether the underlying {@link Preferences} instance was cleared successfully
     */
    public boolean removeAll() {
        try {
            preferences.clear();
        } catch (BackingStoreException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the single instance of this class.
     *
     * @return the one and only instance of this class
     */
    public static Settings getInstance() { return instance; }

    /**
     * Helper class that contains the keys used to store the settings.
     */
    private static final class Keys {
        /** The identifier used for all keys. */
        private static final String BUNDLE_ID = "mhahnFr.SecretPathway";

        /** Key for the hostname or IP address.              */
        public static final String HOSTNAME          = BUNDLE_ID + ".hostname";
        /** Key for the port.                                */
        public static final String PORT              = BUNDLE_ID + ".port";
        /** Key for the width of the main window.            */
        public static final String WINDOW_WIDTH      = BUNDLE_ID + ".windowWidth";
        /** Key for the height of the main window.           */
        public static final String WINDOW_HEIGHT     = BUNDLE_ID + ".windowHeight";
        /** Key for the X-coordinate of the window position. */
        public static final String WINDOW_LOCATION_X = BUNDLE_ID + ".windowLocationX";
        /** Key for the Y-coordinate of the window position. */
        public static final String WINDOW_LOCATION_Y = BUNDLE_ID + ".windowLocationY";
        /** The key used to store the font size.             */
        public static final String FONT_SIZE         = BUNDLE_ID + ".fontSize";
        /** The key used to store the dark mode state.       */
        public static final String DARK_MODE         = BUNDLE_ID + ".darkMode";
    }
}
